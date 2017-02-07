
package dk.dbc;

import com.codahale.metrics.MetricRegistry;
import dk.dbc.rawrepo.oai.formatter.healthchecks.DBHealthCheck;
import dk.dbc.rawrepo.oai.formatter.healthchecks.JavaScriptHealthCheck;
import dk.dbc.rawrepo.oai.formatter.javascript.JavascriptWorkerPool;
import dk.dbc.rawrepo.oai.formatter.configuration.OaiFormatterConfiguration;
import dk.dbc.rawrepo.oai.formatter.resources.OaiFormatterResource;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import javax.sql.DataSource;


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
                        new EnvironmentVariableSubstitutor(false, true)));
    }

    @Override
    public void run(OaiFormatterConfiguration config, Environment env) throws Exception {
        MetricRegistry metrics = env.metrics();
        
        DataSource datasource = config.getDataSourceFactory()
                .build(metrics, getName());
        
        JavascriptWorkerPool jsWorkerPool = new JavascriptWorkerPool(config.getJavaScriptPoolSize());
        
        OaiFormatterResource formatterResource = new OaiFormatterResource(datasource, jsWorkerPool);
        env.jersey().register(formatterResource);
        
        env.healthChecks().register("db", new DBHealthCheck(datasource));
        env.healthChecks().register("jsPool", new JavaScriptHealthCheck(jsWorkerPool));
    }
    
}
