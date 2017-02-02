/*
 * Copyright (C) 2017 DBC A/S (http://dbc.dk/)
 *
 * This is part of dbc-rawrepo-oai-service
 *
 * dbc-rawrepo-oai-service is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * dbc-rawrepo-oai-service is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.rawrepo.oai;

import dk.dbc.oai.pmh.OAIPMH;
import dk.dbc.oai.pmh.OAIPMHerrorType;
import dk.dbc.oai.pmh.OAIPMHerrorcodeType;
import dk.dbc.oai.pmh.ObjectFactory;
import dk.dbc.oai.pmh.RequestType;
import java.io.CharArrayWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.time.ZoneId;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
@Path("")
public class OAIResource {

    private static final Logger log = LoggerFactory.getLogger(OAIResource.class);

    @Resource(lookup = C.DATASOURCE)
    DataSource rawrepo;

    @Inject
    Config config;

    @Inject
    Throttle throttle;

    @Inject
    RecordFormatter recordFormatter;

    @Inject
    AccessControl accessControl;

    private static final JAXBContext context = makeJAXBContext();
    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private static final DatatypeFactory DATATYPE_FACTORY = makeDatatypeFactory();

    private static JAXBContext makeJAXBContext() {
        try {
            return JAXBContext.newInstance(OAIPMH.class);
        } catch (JAXBException ex) {
            throw new EJBException(ex);
        }
    }

    private static DatatypeFactory makeDatatypeFactory() {
        try {
            return DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException ex) {
            throw new EJBException(ex);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response resourcePost(@Context UriInfo uriInfo,
                                 @Context HttpServletRequest req,
                                 MultivaluedMap<String, String> formParams,
                                 @QueryParam("identity") String identityByUrl,
                                 @HeaderParam("Identity") String identityByHeader) {
        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.putAll(uriInfo.getQueryParameters());
        params.putAll(formParams);

        String identity = params.getFirst("identity");
        if (identity == null) {
            identity = identityByUrl;
        }
        if (identity == null) {
            identity = identityByHeader;
        }
        String ip = remoteIp(req);
        return resource(identity, ip, params);
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response resourceGet(@Context UriInfo uriInfo,
                                @Context HttpServletRequest req,
                                @HeaderParam("Identity") String identityByHeader) {

        MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
        String identity = params.getFirst("identity");
        if (identity == null) {
            identity = identityByHeader;
        }
        String ip = remoteIp(req);
        return resource(identity, ip, params);
    }

    /**
     * Process request
     *
     * @param identity user:group:pass | null
     * @param ip       ip-address
     * @param params   request parameters
     * @return response
     */
    private Response resource(String identity, String ip, MultivaluedMap<String, String> params) {
        boolean indent = false;
        String indentParam = params.getFirst("indent");
        if (indentParam != null) {
            switch (indentParam.toLowerCase(Locale.ROOT)) {
                case "1":
                case "on":
                case "true":
                    indent = true;
                    break;
                case "0":
                case "off":
                case "false":
                    indent = false;
                    break;
                default:
                    break;
            }
        }

        String user = ip;
        Set<String> allowedSets = accessControl.getAllSets();
        if (!config.noAuthentication) {
            AccessControl.Response response = accessControl.authenticate(identity, ip);
            user = response.getId();
            allowedSets = response.getAllowedSets();

        }

        OAIPMH oaipmh = OBJECT_FACTORY.createOAIPMH();
        try (AutoCloseable throttleLock = config.noThrottle ? new NoThrottleLock() : throttle.lock(user)) {
            try {
                log.debug("identity = " + identity);
                String verb = params.getFirst("verb");
                if (verb == null) {
                    verb = "";
                }

                try (Connection connection = rawrepo.getConnection() ;
                     PreparedStatement stmt = connection.prepareStatement("SET TIMEZONE TO 'UTC'")) {
                    stmt.executeUpdate();
                    OAIWorker worker = new OAIWorker(connection, config, recordFormatter, allowedSets, params);
                    switch (verb.toLowerCase(Locale.ROOT)) {
                        case "identify":
                            worker.identify(oaipmh);
                            return marshall(oaipmh, indent);
                        case "listmetadataformats":
                            worker.listMetadataFormats(oaipmh);
                            return marshall(oaipmh, indent);
                        case "listsets":
                            worker.listSets(oaipmh);
                            return marshall(oaipmh, indent);
                        case "getrecord":
                            worker.getRecord(oaipmh);
                            return marshall(oaipmh, indent);
                        case "listidentifiers":
                            worker.listIdentifiers(oaipmh);
                            return marshall(oaipmh, indent);
                        case "listrecords":
                            worker.listRecords(oaipmh);
                            return marshall(oaipmh, indent);
                        default:
                            throw new OAIException(OAIPMHerrorcodeType.BAD_VERB, "Unknown verb: " + verb);
                    }
                }
            } catch (OAIException ex) {
                log.info(ex.getMessage());
                logQuery(params);

                List<OAIPMHerrorType> errors = oaipmh.getErrors();
                OAIPMHerrorType error = new OAIPMHerrorType();
                error.setCode(ex.getCode());
                String message = ex.getMessage();
                if (message != null) {
                    error.setValue(message);
                }
                errors.add(error);
                return marshall(oaipmh, indent);
            }
        } catch (ServerErrorException ex) {
            logQuery(params);
            return Response.status(ex.getStatus())
                    .type(MediaType.TEXT_PLAIN)
                    .entity(ex.getMessage())
                    .build();
        } catch (Exception ex) {
            log.error(ex.getMessage());
            logQuery(params);
            log.debug("", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("DON'T PANIC - Error has been logged")
                    .build();
        }
    }

    /**
     * construct a remote ip, using x-forwarded-for header if allowed
     *
     * @param req Header information
     * @return ip number
     */
    private String remoteIp(HttpServletRequest req) {
        String remoteIp = req.getRemoteAddr();
        log.debug("remoteIp = " + remoteIp);
        long ip = ipToLong(remoteIp);
        String forwardedFor = req.getHeader("X-Forwarded-For");
        if (forwardedFor != null) {
            for (String allowedNet : config.xForwardedFor.split(" +")) {
                if (allowedNet.isEmpty()) {
                    continue;
                }

                String[] allowedNetParts = allowedNet.split("/");
                long allowedIp = ipToLong(allowedNetParts[0]);
                long network = allowedNetParts.length == 2 ? Long.parseUnsignedLong(allowedNetParts[1]) : 32;
                network = ( -1 << ( 32 - network ) ) & 0xffffffffL;
                if (( ip & network ) == ( allowedIp & network )) {
                    return forwardedFor.split(",")[0];
                }
            }
        }
        return remoteIp;
    }

    /**
     * converts an ipv4 number into a long
     *
     * @param ipString ip number
     * @return long
     */
    private long ipToLong(String ipString) {
        try {
            long ip = 0;
            for (String string : ipString.split("\\.")) {
                if (!string.isEmpty()) {
                    ip = ( ip << 8 ) | Long.parseUnsignedLong(string);
                }
            }
            return ip;
        } catch (NumberFormatException ex) {
            log.error("Unparsable ip: " + ipString);
            return 0;
        }
    }

    /**
     * Log a query (obfuscating the password)
     *
     * @param params
     */
    private void logQuery(MultivaluedMap<String, String> params) {
        List<String> idetities = params.get("identity");
        if (idetities != null) {
            params.replace("identity", idetities.stream().map(s -> {
                       String[] split = s.split("[:/]");
                       if (split.length >= 3) {
                           s = split[0] + ":" + split[1] + ":********";
                       }
                       return s;
                   }).collect(Collectors.toList()));

        }
        log.debug("query: " + params);
    }

    /**
     * Convert a OAIPMH object to a response
     *
     * Setting schemaLocation
     *
     * @param oaipmh object
     * @param indent pretty print
     * @return Response
     * @throws JAXBException if there's an error in the object structure
     */
    private Response marshall(OAIPMH oaipmh, boolean indent) throws JAXBException {
        XMLGregorianCalendar date = DATATYPE_FACTORY.newXMLGregorianCalendar(new GregorianCalendar());
        oaipmh.setResponseDate(date);
        RequestType request = oaipmh.getRequest();
        if (request != null) {
            request.setValue(config.baseUrl);
        }
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, indent);
        CharArrayWriter writer = new CharArrayWriter();
        marshaller.marshal(oaipmh, writer);
        String value = new String(writer.toCharArray());
        log.debug("Xml is:\n" + value);
        return Response.ok(value).build();
    }

    private static final TimeZone UTC = TimeZone.getTimeZone(ZoneId.of("UTC"));

    /**
     * Construct an gregorian timestamp in UTC timezone
     *
     * @param timestamp then
     * @return timestamp to enter into an oaipmh structure
     */
    public static XMLGregorianCalendar gregorianTimestamp(Instant timestamp) {
        GregorianCalendar gregorian = new GregorianCalendar();
        gregorian.setTimeInMillis(timestamp.toEpochMilli());
        gregorian.setTimeZone(UTC);
        XMLGregorianCalendar date = DATATYPE_FACTORY.newXMLGregorianCalendar(gregorian);
        return date;
    }

    /**
     * Dummy autoclosable, for not locking in throttle object
     */
    private static class NoThrottleLock implements AutoCloseable {

        public NoThrottleLock() {
        }

        @Override
        public void close() throws Exception {
        }
    }

}
