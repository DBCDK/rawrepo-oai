/*
 * Copyright (C) 2017 DBC A/S (http://dbc.dk/)
 *
 * This is part of dbc-rawrepo-oai-formatter
 *
 * dbc-rawrepo-oai-formatter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * dbc-rawrepo-oai-formatter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dk.dbc.rawrepo.oai.formatter;

import dk.dbc.rawrepo.RawRepoDAO;
import dk.dbc.rawrepo.Record;
import dk.dbc.rawrepo.oai.formatter.JavaScriptWorker.OAIFormatUnsupportedException;
import java.sql.Connection;
import java.util.List;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
@Path("")
public class OAIFormatter {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(OAIFormatter.class);
    
    @Resource(lookup = C.DATASOURCE)
    DataSource rawrepo;
    
    @Inject
    Config config;
    
    @Inject
    JavaScriptWorker javaScriptWorker;

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response format(@QueryParam("id") String id, 
                           @QueryParam("format") String format, 
                           @QueryParam("sets") List<String> sets) {
        
        log.debug("Processing request: id={}, format={}, sets={}", id, format, sets);
        
        try (Connection connection = rawrepo.getConnection()) {
            
            FormatRequest request = FormatRequest.parse(id, format, sets);
            
            RawRepoDAO dao = RawRepoDAO.builder(connection).build();
            
            if(!dao.recordExists(request.bibRecId, request.agencyId)){
                throw new NotFoundException();
            }
            
            Record record = dao.fetchRecord(request.bibRecId, request.agencyId);
            String content = new String(record.getContent(), "UTF-8");
            String result = javaScriptWorker.format(content, request.format, request.sets);
            
            return Response.ok(result).build();
            
        } catch (NotFoundException ex) {
            log.debug("Record not found, id={}, format={}, sets={}", id, format, sets);
            return Response.status(Response.Status.NOT_FOUND).entity("No such record: " + id).type(MediaType.TEXT_PLAIN).build();
        } catch (OAIFormatUnsupportedException | IllegalArgumentException ex) {
            log.debug("Invalid request, id={}, format={}, sets={}. Reason={}", id, format, sets, ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(unrollCause(ex)).type(MediaType.TEXT_PLAIN).build();
        } catch (Exception ex) {
            log.error("Could not handle format request id={}, format={}, sets={}. Reason={}", id, format, sets, ex.getMessage());
            log.debug("Could not handle format request id={}, format={}, sets={}", id, format, sets, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(unrollCause(ex)).type(MediaType.TEXT_PLAIN).build();            
        }
    }
    
    public static class FormatRequest {
        
        public final String bibRecId;
        public final int agencyId;
        public final String format;
        public final List<String> sets;
        
        public FormatRequest( String bibRecId, int agencyId, String format, List<String> sets){
            this.bibRecId = bibRecId;
            this.agencyId = agencyId;
            this.format = format;
            this.sets = sets;            
        }
        
        static FormatRequest parse(String id, String format, List<String> sets) {
            
            if(id == null || id.isEmpty()){
                throw new IllegalArgumentException("Missing query param 'id'");
            }

            if(format == null || format.isEmpty()){
                throw new IllegalArgumentException("Missing query param 'format'");
            }

            if(sets ==null || sets.isEmpty()){
                throw new IllegalArgumentException("Missing query param 'sets'");
            }
            
            String bibRecId;
            int agencyId;
            
            try{
                String[] idSplit = id.split(":");
                bibRecId = idSplit[1];
                agencyId = Integer.parseInt(idSplit[0]);
            } catch(RuntimeException e){
                throw new IllegalArgumentException("Illegal value of 'id'. Required format: agencyId:bibRecId");
            }
            
            return new FormatRequest(bibRecId, agencyId, format, sets);
        }
    }
    
    public static String unrollCause( Throwable e ) {
        String message = e.getMessage();

        Throwable cause = null; 
        Throwable result = e;

        while( null != ( cause = result.getCause() )  && ( result != cause ) ) {
            result = cause;
            if( result.getMessage() != null ) {
                message = result.getMessage();
            }
        }
        return message;
    }      

}
