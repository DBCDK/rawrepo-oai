/*
 * Copyright (C) 2017 DBC A/S (http://dbc.dk/)
 *
 * This is part of dbc-rawrepo-oai-harvester-dw
 *
 * dbc-rawrepo-oai-harvester-dw is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * dbc-rawrepo-oai-harvester-dw is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc;

import com.codahale.metrics.MetricRegistry;
import dk.dbc.rawrepo.oai.setmatcher.OaiSetMatcherConfiguration;
import dk.dbc.rawrepo.oai.setmatcher.DBHealthCheck;
import dk.dbc.rawrepo.oai.setmatcher.JavaScriptWorker;
import dk.dbc.rawrepo.oai.setmatcher.RawRepoJobProcessor;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import javax.sql.DataSource;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class Main extends Application<OaiSetMatcherConfiguration> {
    
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
        return "oai-harvester";
    }

    @Override
    public void initialize(Bootstrap<OaiSetMatcherConfiguration> bootstrap) {
        super.initialize(bootstrap);
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(true, true)));
    }

    @Override
    public void run(OaiSetMatcherConfiguration config, Environment env) throws Exception {
        MetricRegistry metrics = env.metrics();
        
        DataSource rawrepo = config.getRawRepoDataSourceFactory()
                .build(metrics, "rawrepo");
        
        DataSource rawrepoOai = config.getRawRepoOaiDataSourceFactory()
                .build(metrics, "rawrepo-oai");
        
        for (int i = 0; i < config.getPoolSize(); i++) {
            new Thread(() -> {
                RawRepoJobProcessor harvester = new RawRepoJobProcessor(rawrepo, rawrepoOai, new JavaScriptWorker());
                harvester.start();
            }).start();            
        }
        
        env.healthChecks().register("rawrepo-db", new DBHealthCheck(rawrepo));
        env.healthChecks().register("rawrepo-oai-db", new DBHealthCheck(rawrepoOai));
    }
    
}
