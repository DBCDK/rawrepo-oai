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

import com.codahale.metrics.MetricRegistry;
import dk.dbc.rawrepo.oai.formatter.healthchecks.JavaScriptHealthCheck;
import dk.dbc.rawrepo.oai.formatter.javascript.JavascriptWorkerPool;
import dk.dbc.rawrepo.oai.formatter.configuration.OaiFormatterConfiguration;
import dk.dbc.rawrepo.oai.formatter.resources.OaiFormatterResource;
import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import javax.ws.rs.client.Client;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class Main extends Application<OaiFormatterConfiguration> {

    public static void main(String[] args) {
        try {
            new Main().run(args);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }

    @Override
    public String getName() {
        return "oai-formatter";
    }

    @Override
    public void initialize(Bootstrap<OaiFormatterConfiguration> bootstrap) {
        super.initialize(bootstrap);
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(true, true)));
    }

    @Override
    public void run(OaiFormatterConfiguration config, Environment env) throws Exception {
        MetricRegistry metrics = env.metrics();

        Client client = new JerseyClientBuilder(env)
                .using(config.getJerseyClientConfiguration())
                .build(getName());

        JavascriptWorkerPool jsWorkerPool = new JavascriptWorkerPool(config.getJavaScriptPoolSize());

        OaiFormatterResource formatterResource =
                new OaiFormatterResource(config.getRawrepoRecordServiceUrl(), client,
                                         jsWorkerPool);
        env.jersey().register(formatterResource);

        env.healthChecks().register("jsPool", new JavaScriptHealthCheck(jsWorkerPool));
    }

}
