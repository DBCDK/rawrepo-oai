/*
 * Copyright (C) 2017 DBC A/S (http://dbc.dk/)
 *
 * This is part of dbc-rawrepo-oai-formatter-dw
 *
 * dbc-rawrepo-oai-formatter-dw is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * dbc-rawrepo-oai-formatter-dw is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.rawrepo.oai.formatter.resources;

import dk.dbc.rawrepo.oai.formatter.javascript.JavascriptWorkerPool;
import dk.dbc.rawrepo.RecordData;
import dk.dbc.rawrepo.RecordServiceConnector;
import dk.dbc.rawrepo.RecordServiceConnectorException;
import dk.dbc.rawrepo.RecordServiceConnectorUnexpectedStatusCodeException;
import dk.dbc.rawrepo.oai.formatter.javascript.JavascriptWorkerPool.JavaScriptWorker;
import dk.dbc.rawrepo.oai.formatter.javascript.MarcXChangeWrapper;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
@Path("/")
public class OaiFormatterResource {

    private static final Logger log = LoggerFactory.getLogger(OaiFormatterResource.class);

    private static final int COMMON_AGENCY = 870970;

    private final RecordServiceConnector connector;
    private final JavascriptWorkerPool jsWorkerPool;

    public OaiFormatterResource(String rawrepoRecordServiceUrl, Client client,JavascriptWorkerPool jsWorkerPool) {
        this.connector = new RecordServiceConnector(client, rawrepoRecordServiceUrl, RecordServiceConnector.TimingLogLevel.DEBUG);
        this.jsWorkerPool = jsWorkerPool;
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response format(@QueryParam("id") String id,
                           @QueryParam("format") String format,
                           @QueryParam("sets") String sets) {
        try {
            FormatRequest request = FormatRequest.parse(id, format, sets);

            if (!connector.recordExists(request.agencyId, request.bibRecId))
                throw new NotFoundException();

            MarcXChangeWrapper[] records = fetchRecordCollection(request.agencyId, request.bibRecId);
            try (JavaScriptWorker jsWorker = jsWorkerPool.borrowWorker()) {
                String result = jsWorker.format(records, request.format, request.sets);
                return Response.ok(result).build();
            }

        } catch (RecordServiceConnectorUnexpectedStatusCodeException ex) {
            log.error("Error getting data from rawrepo-record-service: {}", ex.getMessage());
            log.debug("Error getting data from rawrepo-record-service: ", ex);
        } catch (RecordServiceConnectorException ex) {
            log.error("Error fetching record: {}", ex.getMessage());
            log.debug("Error fetching record: ", ex);
            java.util.logging.Logger.getLogger(OaiFormatterResource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            log.info("Invalid request. Reason={}", ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).type(MediaType.TEXT_PLAIN).build();
        } catch (NotFoundException ex) {
            log.info("Record not found, id={}", id);
            return Response.status(Response.Status.NOT_FOUND).entity("No such record: " + id).type(MediaType.TEXT_PLAIN).build();
        } catch (Exception ex) {
            String cause = unrollCause(ex);
            log.error("Could not handle format request. Reason={}", cause);
            log.debug("Could not handle format request", id, format, sets, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(cause).type(MediaType.TEXT_PLAIN).build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("OK").type(MediaType.TEXT_PLAIN).build();

    }

    /**
     * creates an array consisting of the content of a record + ancestors.
     * Its ordered from closest to most distant ancestor
     *
     * @param agencyId
     * @param bibRecId
     * @param dao
     * @return
     * @throws RawRepoException
     * @throws UnsupportedEncodingException
     */
    MarcXChangeWrapper[] fetchRecordCollection(int agencyId, String bibRecId) throws RecordServiceConnectorException {
        ArrayList<MarcXChangeWrapper> collection = new ArrayList<>();

        boolean cont = true;
        while (cont) {
            cont = false;

            String parentContent = new String(connector.getRecordContent(agencyId, bibRecId), StandardCharsets.UTF_8);
            RecordData.RecordId[] children = connector.getRecordChildren(agencyId, bibRecId);
            children = Arrays.stream(children)
                    .filter(id -> id.getAgencyId() == agencyId)
                    .toArray(RecordData.RecordId[]::new);
            collection.add(new MarcXChangeWrapper(parentContent, children));

            RecordData.RecordId[] parents = connector.getRecordParents(agencyId, bibRecId);
            for (RecordData.RecordId parent : parents) {
                if (parent.getAgencyId() == agencyId) {
                    bibRecId = parent.getBibliographicRecordId();
                    cont = true;
                    break;
                }
            }
        }
        return collection.toArray(new MarcXChangeWrapper[collection.size()]);
    }

    public static class FormatRequest {

        public final String bibRecId;
        public final int agencyId;
        public final String format;
        public final List<String> sets;

        public FormatRequest(String bibRecId, int agencyId, String format, List<String> sets) {
            this.bibRecId = bibRecId;
            this.agencyId = agencyId;
            this.format = format;
            this.sets = sets;
        }

        static FormatRequest parse(String id, String format, String sets) {

            if (id == null || id.isEmpty())
                throw new IllegalArgumentException("Missing query param 'id'");

            if (format == null || format.isEmpty())
                throw new IllegalArgumentException("Missing query param 'format'");

            if (sets == null || sets.isEmpty())
                throw new IllegalArgumentException("Missing query param 'sets'");

            String bibRecId;
            int agencyId;

            try {
                String[] idSplit = id.split(":");
                bibRecId = idSplit[1];
                agencyId = Integer.parseInt(idSplit[0]);
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Illegal value of 'id'. Required format: agencyId:bibRecId");
            }

            return new FormatRequest(bibRecId, agencyId, format, Arrays.asList(sets.split(",")));
        }
    }

    public static String unrollCause(Throwable e) {
        String message = e.getMessage();

        Throwable cause = null;
        Throwable result = e;

        while (null != ( cause = result.getCause() ) && result != cause) {
            result = cause;
            if (result.getMessage() != null)
                message = result.getMessage();
        }
        return message;
    }

}
