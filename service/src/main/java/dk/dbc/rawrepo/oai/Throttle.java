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

import java.time.Instant;
import java.util.HashMap;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
@Singleton
@Startup
//@Lock(LockType.WRITE) // Implicit
public class Throttle {

    private static final Logger log = LoggerFactory.getLogger(Throttle.class);

//  Since @Lock(WRITE) ConcurrentHashMap isn't necessary
//  private static final ConcurrentHashMap<String, Instant> AGENCIES = new ConcurrentHashMap<>();
    private static final HashMap<String, Instant> AGENCIES = new HashMap<>();

    /**
     * Construct a lock on agency level
     *
     * @param agency
     * @return lock
     */
    public AutoCloseable lock(String agency) {
        return new Lock(agency);
    }

    private static class Lock implements AutoCloseable {

        private final String agency;

        private Lock(String agency) {
            this.agency = agency;
            Instant started = AGENCIES.putIfAbsent(agency, Instant.now());
            if (started != null) {
                log.debug("Agency '" + agency + "' already running. Started at: " + started);
                throw new ServerErrorException(Response.Status.SERVICE_UNAVAILABLE, "No concurrent requestsd allowed");
            }
        }

        @Override
        public void close() {
            AGENCIES.remove(agency);
        }
    }
}
