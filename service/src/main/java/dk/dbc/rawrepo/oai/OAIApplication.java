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

import com.codahale.metrics.MetricRegistry;
import dk.dbc.DbcApplication;
import io.dropwizard.setup.Environment;
import javax.sql.DataSource;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class OAIApplication extends DbcApplication<OAIConfiuration> {

    @Override
    public String getName() {
        return "oai-service";
    }

    @Override
    public void run(OAIConfiuration config, Environment env) throws Exception {

        System.out.println("formatService = " + config.getFormatService());

        MetricRegistry metrics = env.metrics();
        DataSource datasource = config.getDataSourceFactory()
                .build(metrics, getName());

        Throttle throttle = new Throttle();
        RecordFormatter recordFormatter = new RecordFormatter(config, metrics);
        AccessControl accessControl = new AccessControl(config, datasource, metrics);
        OAIResource oaiResource = new OAIResource(config, datasource, accessControl, throttle, recordFormatter);
        env.jersey().register(oaiResource);

        ThrottleResource throttleResource = new ThrottleResource(throttle);
        env.jersey().register(throttleResource);

        env.healthChecks().register("db", new DBHealthCheck(datasource));
    }

}
