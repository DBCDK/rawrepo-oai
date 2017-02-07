/*
 * Copyright (C) 2017 DBC A/S (http://dbc.dk/)
 *
 * This is part of dbc-rawrepo-oai-service-dw
 *
 * dbc-rawrepo-oai-service-dw is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * dbc-rawrepo-oai-service-dw is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.rawrepo.oai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
@Path("/throttle")
public class ThrottleResource {

    private static final Logger log = LoggerFactory.getLogger(ThrottleResource.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final Throttle throttle;

    public ThrottleResource(Throttle throttle) {
        this.throttle = throttle;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getThrottle() {
        Map<String, Instant> map = throttle.getMap();
        try {
            return Response.ok(OBJECT_MAPPER.writeValueAsString(map)).build();
        } catch (JsonProcessingException ex) {
            log.error("Exception: " + ex.getMessage());
            log.debug("Exception:", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Internal Error\n")
                    .build();
        }
    }
}
