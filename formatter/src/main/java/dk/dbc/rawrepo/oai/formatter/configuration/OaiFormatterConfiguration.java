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
package dk.dbc.rawrepo.oai.formatter.configuration;

import dk.dbc.DbcConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientConfiguration;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class OaiFormatterConfiguration extends DbcConfiguration {

    @NotNull
    private Integer javaScriptPoolSize;

    @JsonProperty("javaScriptPoolSize")
    public Integer getJavaScriptPoolSize() {
        return javaScriptPoolSize;
    }

    @JsonProperty("javaScriptPoolSize")
    public void setJavaScriptPoolSize(Integer javaScriptPoolSize) {
        this.javaScriptPoolSize = javaScriptPoolSize;
    }

    @NotNull
    private String rawrepoRecordServiceUrl;

    @JsonProperty("rawrepoRecordServiceUrl")
    public String getRawrepoRecordServiceUrl() {
        return rawrepoRecordServiceUrl;
    }

    @JsonProperty("rawrepoRecordServiceUrl")
    public void setRawrepoRecordServiceUrl(String rawrepoRecordServiceUrl) {
        this.rawrepoRecordServiceUrl = rawrepoRecordServiceUrl;
    }

    @Valid
    @NotNull
    private JerseyClientConfiguration httpClient = new JerseyClientConfiguration();

    @JsonProperty("restClient")
    public void setJerseyClientConfiguration(JerseyClientConfiguration httpClient) {
        this.httpClient = httpClient;
    }

    @JsonProperty("restClient")
    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return httpClient;
    }

}
