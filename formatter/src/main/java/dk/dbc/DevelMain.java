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
package dk.dbc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableMultimap;
import dk.dbc.rawrepo.oai.formatter.configuration.OaiFormatterConfiguration;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class DevelMain extends Main {

    private static final Logger log = LoggerFactory.getLogger(DevelMain.class);

    private static final String YAML_FILE_NAME = "src/test/resources/config_devel.yaml";
    private static final String LOGBACK_XML = "src/test/resources/logback_devel.xml";

    public static void main(String[] args) {
        try {
            System.setProperty("logback.configurationFile", LOGBACK_XML);
            new DevelMain().run(new String[] {"server", YAML_FILE_NAME});
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void initialize(Bootstrap<OaiFormatterConfiguration> bootstrap) {
        try {
            CloseableHttpClient client = new HttpClientBuilder(bootstrap.getMetricRegistry())
                    .build("client");
            String killUrl = findAdminUrl(YAML_FILE_NAME) + "tasks/terminate";
            HttpPost post = new HttpPost(killUrl);
            post.setEntity(new StringEntity("", ContentType.APPLICATION_FORM_URLENCODED));
            try (CloseableHttpResponse resp = client.execute(post) ;
                 InputStream is = resp.getEntity().getContent()) {
                String content = readInputStream(is);
                log.debug(content);
            }
            Thread.sleep(100);
        } catch (InterruptedException | IOException ex) {
            log.info(ex.getMessage());
        }
        super.initialize(bootstrap);
    }

    @Override
    public void run(OaiFormatterConfiguration config, Environment env) throws Exception {
        super.run(config, env);
        env.admin().addTask(new Task("terminate") {
            @Override
            @SuppressFBWarnings(value = {"BAD_PRACTICE", "DM_EXIT"}, justification = "It's intentional that the system should exit on a 'terminate' call")
            public void execute(ImmutableMultimap<String, String> im, PrintWriter writer) throws Exception {
                writer.append("EXITTING\n");
                writer.close();
                new Thread() {
                    @Override
                    public void run() {
                        log.info("EXITTING");
                        System.exit(0);
                    }
                }.start();
            }
        });
    }

    private static String findAdminUrl(String yamlFileName) throws IOException {
        String port = System.getProperty("dw.server.adminConnectors[0].port");

        if (port != null) {
            port = "http://localhost:" + port + "/";
        } else {
            try (FileInputStream is = new FileInputStream(yamlFileName)) {
                String yaml = readInputStream(is);

                ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
                Map<String, Object> obj = (Map<String, Object>) yamlReader.readValue(yaml, Object.class);
                log.debug("obj = " + obj);
                if (obj.get("server") != null && obj.get("server") instanceof Map) {
                    Map<String, Object> server = (Map<String, Object>) obj.get("server");
                    if (server.get("adminConnectors") != null && server.get("adminConnectors") instanceof List) {
                        List<Object> adminConnectors = (List< Object>) server.get("adminConnectors");
                        Map<String, Object> set = adminConnectors.stream()
                                .filter(e -> e instanceof Map)
                                .map(e -> (Map<String, Object>) e)
                                .filter(e -> "http".equals(e.get("type")))
                                .findFirst()
                                .orElse(null);
                        port = String.valueOf(set.get("type")) + "://localhost:" + String.valueOf(set.get("port")) + "/";
                    }
                }
            }
        }
        if (port == null) {
            port = "http://localhost:8080/";
        }
        return port;
    }

    private static String readInputStream(final InputStream is) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            while (is.available() > 0) {
                int read = is.read(buffer);
                if (read > 0) {
                    bos.write(buffer, 0, read);
                }
            }
            String s = new String(bos.toByteArray(), StandardCharsets.UTF_8);
            return s;
        }
    }

}
