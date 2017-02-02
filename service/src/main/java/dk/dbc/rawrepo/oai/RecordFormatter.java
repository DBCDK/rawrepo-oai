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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import java.util.concurrent.Future;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
@Stateless
public class RecordFormatter {

    private static final Logger log = LoggerFactory.getLogger(RecordFormatter.class);

    @Inject
    Timer recordsFetchedTotal;

    @Inject
    Counter recordsNotFetchedDeleted;

    @Inject
    Counter recordsFetchedFailed;

    public static class RecordWithContent {

        private final OAIIdentifier identifier;
        private final String content;

        private RecordWithContent(OAIIdentifier identifier, String content) {
            this.identifier = identifier;
            this.content = content;
        }

        /**
         * Identifier
         *
         * @return identifier
         */
        public OAIIdentifier getIdentifier() {
            return identifier;
        }

        /**
         * Xml content
         *
         * @return content (raw xml)
         */
        public String getContent() {
            return content;
        }
    }

    /**
     * Fetch a record
     *
     * @param identifier     what to get
     * @param metadataPrefix fetch of record
     * @return RecordWithContent (OAIIdentifier, String)
     */
    @Asynchronous
    public Future<RecordWithContent> fetch(OAIIdentifier identifier, String metadataPrefix) {
        if (identifier.isDeleted()) {
            log.info("Not fetching. record is deleted");
            return new AsyncResult<>(new RecordWithContent(identifier, null));
        }
        try (Timer.Context time = recordsFetchedTotal.time()) {
            try {
                log.info("Pre Sleep");
                Thread.sleep(1000);
                log.info("Post Sleep");
            } catch (InterruptedException ex) {
            }
            return new AsyncResult<>(new RecordWithContent(identifier, "<record>" + identifier.getIdentifier() + "</record>"));
        }
    }
}
