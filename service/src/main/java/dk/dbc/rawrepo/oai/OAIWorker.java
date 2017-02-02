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

import dk.dbc.oai.pmh.DeletedRecordType;
import dk.dbc.oai.pmh.DescriptionType;
import dk.dbc.oai.pmh.GetRecordType;
import dk.dbc.oai.pmh.GranularityType;
import dk.dbc.oai.pmh.HeaderType;
import dk.dbc.oai.pmh.IdentifyType;
import dk.dbc.oai.pmh.ListIdentifiersType;
import dk.dbc.oai.pmh.ListMetadataFormatsType;
import dk.dbc.oai.pmh.ListRecordsType;
import dk.dbc.oai.pmh.ListSetsType;
import dk.dbc.oai.pmh.MetadataFormatType;
import dk.dbc.oai.pmh.MetadataType;
import dk.dbc.oai.pmh.OAIPMH;
import dk.dbc.oai.pmh.OAIPMHerrorType;
import dk.dbc.oai.pmh.OAIPMHerrorcodeType;
import dk.dbc.oai.pmh.ObjectFactory;
import dk.dbc.oai.pmh.RecordType;
import dk.dbc.oai.pmh.RequestType;
import dk.dbc.oai.pmh.SetType;
import dk.dbc.oai.pmh.VerbType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class OAIWorker {

    private static final Logger log = LoggerFactory.getLogger(OAIWorker.class);

    private static final String DC_PRE = "<oai_dc:dc " +
                                         "xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" " +
                                         "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " +
                                         "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                                         "xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ " +
                                         "http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">" +
                                         "<dc:description>";
    private static final String DC_POST = "</dc:description>" +
                                          "</oai_dc:dc>";

    private static final DocumentBuilder DOCUMENT_BUILDER = newDocumentBuilder();
    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private static final Timestamp EPOCH = new Timestamp(0);

    private final Connection connection;
    private final Config config;
    private final RecordFormatter recordFormatter;
    private final Set<String> allowedSets;
    private final MultivaluedMap<String, String> params;

    private static DocumentBuilder newDocumentBuilder() {
        synchronized (DocumentBuilderFactory.class) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                dbf.setIgnoringComments(true);
                dbf.setIgnoringElementContentWhitespace(true);
                DocumentBuilder db = dbf.newDocumentBuilder();
                db.setEntityResolver(new NullResolver());
                return db;
            } catch (ParserConfigurationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public OAIWorker(Connection connection,
                     Config config,
                     RecordFormatter recordFormatter,
                     Set<String> allowedSets,
                     MultivaluedMap<String, String> params) {
        this.connection = connection;
        this.config = config;
        this.recordFormatter = recordFormatter;
        this.allowedSets = allowedSets;
        this.params = params;
    }

    /**
     * http://www.openarchives.org/OAI/openarchivesprotocol.html#GetRecord
     *
     * @param oaipmh response object
     */
    public void getRecord(OAIPMH oaipmh) {
        RequestType request = setRequest(oaipmh, VerbType.GET_RECORD);
        String identifier = getParameterRequired("identifier", s -> request.setIdentifier(s));
        String metadataPrefix = getParameterRequired("metadataPrefix", s -> request.setMetadataPrefix(s));

        try {
            GetRecordType getRecord = OBJECT_FACTORY.createGetRecordType();

            OAIIdentifier oaiIdentifier = OAIIdentifier.fromDb(connection, identifier);
            List<RecordType> records = fetchRecordContent(Arrays.asList(oaiIdentifier), metadataPrefix);
            if (records.isEmpty()) {
                log.error("Somehow the record: " + identifier + " doesn't get content");
                throw new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR, "Internal Error");
            }

            getRecord.setRecord(records.get(0));
            oaipmh.setGetRecord(getRecord);
        } catch (OAIException ex) {
            List<OAIPMHerrorType> errors = oaipmh.getErrors();
            OAIPMHerrorType error = new OAIPMHerrorType();
            error.setCode(ex.getCode());
            error.setValue(ex.getMessage());
            errors.add(error);
        }
    }

    /**
     * http://www.openarchives.org/OAI/openarchivesprotocol.html#Identify
     *
     * @param oaipmh response object
     */
    public void identify(OAIPMH oaipmh) {
        setRequest(oaipmh, VerbType.IDENTIFY);

        Timestamp timestamp = EPOCH;
        try (PreparedStatement stmt = connection.prepareStatement("SELECT MIN(changed) FROM oairecords") ;
             ResultSet resultSet = stmt.executeQuery()) {
            if (resultSet.next()) {
                timestamp = resultSet.getTimestamp(1);
            }
        } catch (SQLException ex) {
            log.error("Exception: " + ex.getMessage());
            log.debug("Exception:", ex);
            throw new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR, "Internal Error");
        }

        String ts = DateTimeFormatter.ISO_INSTANT.format(timestamp.toInstant());

        IdentifyType identify = OBJECT_FACTORY.createIdentifyType();
        identify.setRepositoryName(config.repositoryName);
        identify.setBaseURL(config.baseUrl);
        identify.setProtocolVersion("2.0");
        identify.setEarliestDatestamp(ts);
        identify.setDeletedRecord(DeletedRecordType.TRANSIENT);
        identify.setGranularity(GranularityType.YYYY_MM_DD_THH_MM_SS_Z);
        oaipmh.setIdentify(identify);
    }

    /**
     * http://www.openarchives.org/OAI/openarchivesprotocol.html#ListIdentifiers
     *
     * @param oaipmh response object
     */
    public void listIdentifiers(OAIPMH oaipmh) {
        setRequest(oaipmh, VerbType.LIST_IDENTIFIERS);

        OAIIdentifierCollection collection = new OAIIdentifierCollection(connection, allowedSets);
        JsonObject json = findIdentifiersJson(oaipmh);
        JsonObject cont = collection.fetch(json, config.identifiersPrRequest);

        ListIdentifiersType listIdetifiers = OBJECT_FACTORY.createListIdentifiersType();
        List<HeaderType> headers = listIdetifiers.getHeaders();

        collection.stream()
                .forEach(id -> {
                    headers.add(id.toHeader());
                });

        listIdetifiers.setResumptionToken(ResumptionToken.toToken(cont, config.tokenMaxAge));

        oaipmh.setListIdentifiers(listIdetifiers);
    }

    /**
     * http://www.openarchives.org/OAI/openarchivesprotocol.html#ListMetadataFormats
     *
     * @param oaipmh response object
     */
    public void listMetadataFormats(OAIPMH oaipmh) {
        RequestType request = setRequest(oaipmh, VerbType.LIST_METADATA_FORMATS);
        getParameter("identifier", s -> request.setIdentifier(s));

        ListMetadataFormatsType listMetadataFormats = OBJECT_FACTORY.createListMetadataFormatsType();
        List<MetadataFormatType> metadataFormats = listMetadataFormats.getMetadataFormats();

        try (PreparedStatement stmt = connection.prepareStatement("SELECT prefix, schema, namespace FROM oaiformats") ;
             ResultSet resultSet = stmt.executeQuery()) {
            while (resultSet.next()) {
                MetadataFormatType format = OBJECT_FACTORY.createMetadataFormatType();
                format.setMetadataPrefix(resultSet.getString(1));
                format.setSchema(resultSet.getString(2));
                format.setMetadataNamespace(resultSet.getString(3));
                metadataFormats.add(format);
            }
        } catch (SQLException ex) {
            log.error("Exception: " + ex.getMessage());
            log.debug("Exception:", ex);
            throw new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR, "Internal Error");
        }
        oaipmh.setListMetadataFormats(listMetadataFormats);
    }

    /**
     * http://www.openarchives.org/OAI/openarchivesprotocol.html#ListRecords
     *
     * @param oaipmh response object
     */
    public void listRecords(OAIPMH oaipmh) {
        setRequest(oaipmh, VerbType.LIST_IDENTIFIERS);
        ListRecordsType listRecords = OBJECT_FACTORY.createListRecordsType();
        OAIIdentifierCollection collection = new OAIIdentifierCollection(connection, allowedSets);
        JsonObject json = findIdentifiersJson(oaipmh);
        JsonObject cont = collection.fetch(json, config.recordsPrRequest);

        List<RecordType> recordsWithContent = fetchRecordContent(collection, "marcx");

        List<RecordType> records = listRecords.getRecords();
        records.addAll(recordsWithContent);

        listRecords.setResumptionToken(ResumptionToken.toToken(cont, config.tokenMaxAge));
        oaipmh.setListRecords(listRecords);
    }

    /**
     * http://www.openarchives.org/OAI/openarchivesprotocol.html#ListSets
     *
     * @param oaipmh response object
     */
    public void listSets(OAIPMH oaipmh) {
        RequestType request = setRequest(oaipmh, VerbType.LIST_SETS);
        String resumptionToken = getParameter("resumptionToken", s -> request.setResumptionToken(s));
        if (resumptionToken != null) {
            throw new OAIException(OAIPMHerrorcodeType.BAD_RESUMPTION_TOKEN, "Unknown resumption token");
        }
        ListSetsType listSets = OBJECT_FACTORY.createListSetsType();
        List<SetType> sets = listSets.getSets();

        try (PreparedStatement stmt = connection.prepareStatement("SELECT setSpec, setName, description FROM oaisets") ;
             ResultSet resultSet = stmt.executeQuery()) {
            while (resultSet.next()) {
                SetType set = OBJECT_FACTORY.createSetType();
                set.setSetSpec(resultSet.getString(1));
                set.setSetName(resultSet.getString(2));
                String desc = resultSet.getString(3);
                if (desc != null) {
                    DescriptionType description = OBJECT_FACTORY.createDescriptionType();
                    description.setAny(stringToElement(DC_PRE + desc + DC_POST));
                    set.getSetDescriptions().add(description);
                }
                sets.add(set);
            }
        } catch (SQLException ex) {
            log.error("Exception: " + ex.getMessage());
            log.debug("Exception:", ex);
            throw new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR, "Internal Error");
        }
        oaipmh.setListSets(listSets);
    }

    /**
     * Take an OAIIdentifier list and convert into a Record list, with the
     * content formattet as medataPrefix
     *
     * @param collection     identifiers to fetch
     * @param metadataPrefix fetch to get content in
     * @return list of records
     */
    private List<RecordType> fetchRecordContent(List<OAIIdentifier> collection, String metadataPrefix) {
        List<Future<RecordFormatter.RecordWithContent>> futures = collection.stream()
                .map(id -> recordFormatter.fetch(id, metadataPrefix))
                .collect(Collectors.toList());
        try {
            Instant timeout = Instant.now().plus(config.fetchRecordTimeout, ChronoUnit.SECONDS);
            List<RecordType> recordsWithContent = futures.stream()
                    .map(future -> {
                        try {
                            long until = Math.max(0, Instant.now().until(timeout, ChronoUnit.SECONDS));
                            RecordFormatter.RecordWithContent rec = future.get(until, TimeUnit.SECONDS);
                            RecordType record = rec.getIdentifier().toRecord();
                            if (!rec.getIdentifier().isDeleted()) {
                                MetadataType metadata = OBJECT_FACTORY.createMetadataType();
                                metadata.setAny(stringToElement(rec.getContent()));
                                record.setMetadata(metadata);
                            }
                            return record;
                        } catch (InterruptedException | ExecutionException ex) {
                            log.error("Exception: " + ex.getMessage());
                            log.debug("Exception: ", ex);
                            throw new ServerErrorException(Response.Status.REQUEST_TIMEOUT, "Error retrieving record");
                        } catch (TimeoutException ex) {
                            log.error("Exception: " + ex.getMessage());
                            log.debug("Exception: ", ex);
                            throw new ServerErrorException(Response.Status.REQUEST_TIMEOUT, "Error waiting for record formatting");
                        }
                    })
                    .collect(Collectors.toList());
            return recordsWithContent;
        } catch (Exception e) {
            futures.stream().forEach(future -> {
                try {
                    future.cancel(true);
                } catch (Exception ex) {
                    log.debug("Error canceling request: " + ex.getMessage());
                }
            });
            throw e;
        }
    }

    /**
     * Convert a resumption token or request to Json structure user for fetching
     * records
     *
     * @param oaipmh Output object for storing request arguments
     * @return json
     */
    private JsonObject findIdentifiersJson(OAIPMH oaipmh) {
        RequestType request = oaipmh.getRequest();
        String token = getParameter("resumptionToken", s -> request.setResumptionToken(s));
        if (token != null) {
            return ResumptionToken.decode(token);
        } else {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            getParameterRequired("metadataPrefix", s -> {
                             request.setMetadataPrefix(s);
                             ob.add("m", s);
                         });
            getParameter("from", s -> {
                     request.setFrom(s);
                     ob.add("f", s);
                 });
            getParameter("until", s -> {
                     request.setFrom(s);
                     ob.add("u", s);
                 });
            getParameter("set", s -> {
                     if (!allowedSets.contains(s)) {
                         throw new OAIException(OAIPMHerrorcodeType.BAD_ARGUMENT, "Unknown/Forbidden set");
                     }
                     request.setFrom(s);
                     ob.add("s", s);
                 });
            return ob.build();
        }
    }

    /**
     * Construct a OAIPMH top level request object
     *
     * @param oaipmh   where to store the request
     * @param verbType type of the request
     * @return the request object
     */
    private RequestType setRequest(OAIPMH oaipmh, VerbType verbType) {
        RequestType request = OBJECT_FACTORY.createRequestType();
        request.setVerb(verbType);
        oaipmh.setRequest(request);
        return request;
    }

    /**
     * Get a request parameter and fail if it isn't set
     *
     * @param name   name of the parameter
     * @param action what to do with the parameter (null is ok)
     * @return content of parameter
     */
    private String getParameterRequired(String name, Consumer<String> action) {
        String value = getParameter(name, action);
        if (value == null) {
            throw new OAIException(OAIPMHerrorcodeType.BAD_ARGUMENT, name + " is not set");
        }
        return value;
    }

    /**
     * Get a request parameter
     *
     * @param name   name of the parameter
     * @param action what to do with the parameter (null is ok)
     * @return content of parameter
     */
    private String getParameter(String name, Consumer<String> action) {
        String value = params.getFirst(name);
        if (value != null && action != null) {
            action.accept(value);
        }
        return value;

    }

    /**
     * Convert a text to an xml element.
     *
     * @param text xml document
     * @return xml element for placing in an any
     */
    private static Element stringToElement(String text) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
            return DOCUMENT_BUILDER.parse(bis).getDocumentElement();
        } catch (SAXException | IOException ex) {
            log.error("Exception: " + ex.getMessage());
            log.debug("Exception:", ex);
            throw new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR, "Internal Error");

        }
    }

    /**
     * Resolver for document builder
     */
    private static class NullResolver implements EntityResolver {

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            return new InputSource(new StringReader(""));
        }
    }
}
