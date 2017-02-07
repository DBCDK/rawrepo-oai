package dk.dbc.rawrepo.oai.formatter.configuration;

import dk.dbc.DbcConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.db.DataSourceFactory;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class OaiFormatterConfiguration extends DbcConfiguration {
    
    @Valid
    @NotNull
    private DataSourceFactory database;
    
    @NotNull
    private Integer javaScriptPoolSize;

    @JsonProperty("rawrepo")
    public void setDataSourceFactory(DataSourceFactory factory) {
        this.database = factory;
    }

    @JsonProperty("rawrepo")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("javaScriptPoolSize")
    public Integer getJavaScriptPoolSize() {
        return javaScriptPoolSize;
    }

    @JsonProperty("javaScriptPoolSize")
    public void setJavaScriptPoolSize(Integer javaScriptPoolSize) {
        this.javaScriptPoolSize = javaScriptPoolSize;
    }
    
}
