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
import com.codahale.metrics.MetricRegistry;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class RecordFormatter {

    private static final Logger log = LoggerFactory.getLogger(RecordFormatter.class);

    private final OAIConfiuration config;
    private final Client client;
    private final Counter records;
    private final Counter deleted;

    public RecordFormatter(OAIConfiuration config, MetricRegistry metrics, Client client) {
        this.config = config;
        this.client = client;
        this.records = metrics.counter(getClass().getCanonicalName() + "/records");
        this.deleted = metrics.counter(getClass().getCanonicalName() + "/deleted");
    }

    public static class RecordWithContent {

        private final OAIIdentifier oaiIdentifier;
        private final Future<Response> response;
        private String content;

        public RecordWithContent(OAIIdentifier oaiIdentifier, Future<Response> response) {
            this.oaiIdentifier = oaiIdentifier;
            this.response = response;
            this.content = null;
        }

        /**
         * Identifier
         *
         * @return identifier
         */
        public OAIIdentifier getOAIIdentifier() {
            return oaiIdentifier;
        }

        public void complete(long seconds) throws InterruptedException, ExecutionException, TimeoutException {
            if (response != null) {
                Response resp = response.get(10, TimeUnit.SECONDS);
                if (resp.getStatus() != 200) {
                    throw new RuntimeException("Error fetching record: " + resp.getStatusInfo());
                }
                content = resp.readEntity(String.class);
            }
        }

        public String getContent() {
            return content;
        }

        public void cancel(boolean n) {
            if (response != null &&
                !response.isDone() &&
                !response.isCancelled()) {
                response.cancel(n);
            }
        }

    }
    private static final Pattern ENV_MATCHER = Pattern.compile("\\%\\((\\w+)\\)");

    /**
     * Fetch a record
     *
     * @param identifier     what to get
     * @param metadataPrefix fetch of record
     * @param allowedSets    what sets a client is allowed to see
     * @return RecordWithContent (OAIIdentifier, String)
     */
    public RecordWithContent fetch(OAIIdentifier identifier, String metadataPrefix, Set<String> allowedSets) {
        records.inc();
        if (identifier.isDeleted()) {
            deleted.inc();
            return new RecordWithContent(identifier, null);
        }
        try {
            StringBuffer sb = new StringBuffer();
            Matcher matcher = ENV_MATCHER.matcher(config.getFormatService());
            while (matcher.find()) {
                String content;
                switch (matcher.group(1)) {
                    case "id":
                        content = identifier.getIdentifier();
                        break;
                    case "format":
                        content = metadataPrefix;
                        break;
                    case "sets":
                        content = String.join(",", allowedSets);
                        break;
                    default:
                        content = "";
                        break;
                }
                matcher.appendReplacement(sb, URLEncoder.encode(content, "UTF-8"));
            }
            matcher.appendTail(sb);
            String request = sb.toString();
            log.debug("request = " + request);
            Future<Response> resp = client.target(request)
                    .request()
                    .async()
                    .get();
            return new RecordWithContent(identifier, resp);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
