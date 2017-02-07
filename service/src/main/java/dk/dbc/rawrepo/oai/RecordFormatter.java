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
import com.codahale.metrics.Timer;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class RecordFormatter {

    private static final Logger log = LoggerFactory.getLogger(RecordFormatter.class);

    private final OAIConfiuration config;
    private final ExecutorService threadPool;
    private final Counter records;
    private final Counter deleted;
    private final Timer fetching;

    public RecordFormatter(OAIConfiuration config, MetricRegistry metrics) {
        this.config = config;
        this.records = metrics.counter(getClass().getCanonicalName() + "/records");
        this.deleted = metrics.counter(getClass().getCanonicalName() + "/deleted");
        this.fetching = metrics.timer(getClass().getCanonicalName() + "/fetching");
        this.threadPool = Executors.newFixedThreadPool(10);
    }


    public static class RecordWithContent {

        private final OAIIdentifier oaiIdentifier;
        private final String content;

        private RecordWithContent(OAIIdentifier oaiIdentifier, String content) {
            this.oaiIdentifier = oaiIdentifier;
            this.content = content;
        }

        /**
         * Identifier
         *
         * @return identifier
         */
        public OAIIdentifier getOAIIdentifier() {
            return oaiIdentifier;
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
    private static final Pattern ENV_MATCHER = Pattern.compile("\\$\\{(\\w+)\\}");

    /**
     * Fetch a record
     *
     * @param identifier     what to get
     * @param metadataPrefix fetch of record
     * @param allowedSets    what sets a client is allowed to see
     * @return RecordWithContent (OAIIdentifier, String)
     */
    public Future<RecordWithContent> fetch(OAIIdentifier identifier, String metadataPrefix, Set<String> allowedSets) {
        return threadPool.submit(new Callable<RecordWithContent>() {
            @Override
            public RecordWithContent call() throws Exception {
                records.inc();
                if (identifier.isDeleted()) {
                    deleted.inc();
                    log.info("Not fetching. record is deleted");
                    return new RecordWithContent(identifier, null);
                }
                try (Timer.Context time = fetching.time()) {
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

                    log.info("Fetching record: " + request);
                    Client client = ClientBuilder.newClient();
                    String content = client.target(request)
                            .request(MediaType.APPLICATION_XML)
                            .get(String.class);
                    log.trace("content for " + identifier.getIdentifier() + " is: " + content);
                    return new RecordWithContent(identifier, content);
                } catch (RuntimeException | UnsupportedEncodingException ex) {
                    log.error("Exception: " + ex.getMessage());
                    log.debug("Exception: ", ex);
                    return new RecordWithContent(identifier, null);
                }
            }
        });
    }
}
