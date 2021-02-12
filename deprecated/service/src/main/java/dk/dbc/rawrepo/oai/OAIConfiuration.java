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

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.dbc.DbcConfiguration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class OAIConfiuration extends DbcConfiguration {

    private String repositoryName;
    private String baseUrl;
    private int recordsPrRequest;
    private int identifiersPrRequest;
    private int tokenMaxAge;
    private int fetchRecordsTimeout;
    private boolean noThrottle;
    private boolean noAuthentication;
    private String xForwardedFor;
    private String forsRightsUrl;
    private String forsRightsRules;
    private String formatService;

    @JsonProperty
    public String getRepositoryName() {
        return repositoryName;
    }

    @JsonProperty
    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    @JsonProperty
    public String getBaseUrl() {
        return baseUrl;
    }

    @JsonProperty
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @JsonProperty
    public int getRecordsPrRequest() {
        return recordsPrRequest;
    }

    @JsonProperty
    public void setRecordsPrRequest(int recordsPrRequest) {
        this.recordsPrRequest = recordsPrRequest;
    }

    @JsonProperty
    public int getIdentifiersPrRequest() {
        return identifiersPrRequest;
    }

    @JsonProperty
    public void setIdentifiersPrRequest(int identifiersPrRequest) {
        this.identifiersPrRequest = identifiersPrRequest;
    }

    @JsonProperty
    public int getTokenMaxAge() {
        return tokenMaxAge;
    }

    @JsonProperty
    public void setTokenMaxAge(int tokenMaxAge) {
        this.tokenMaxAge = tokenMaxAge;
    }

    @JsonProperty
    public int getFetchRecordsTimeout() {
        return fetchRecordsTimeout;
    }

    @JsonProperty
    public void setFetchRecordsTimeout(int fetchRecordsTimeout) {
        this.fetchRecordsTimeout = fetchRecordsTimeout;
    }

    @JsonProperty
    public boolean getNoThrottle() {
        return noThrottle;
    }

    @JsonProperty
    public void setNoThrottle(boolean noThrottle) {
        this.noThrottle = noThrottle;
    }

    @JsonProperty
    public boolean getNoAuthentication() {
        return noAuthentication;
    }

    @JsonProperty
    public void setNoAuthentication(boolean noAuthentication) {
        this.noAuthentication = noAuthentication;
    }

    @JsonProperty
    public String getxForwardedFor() {
        return xForwardedFor;
    }

    @JsonProperty
    public void setxForwardedFor(String xForwardedFor) {
        this.xForwardedFor = xForwardedFor;
    }

    @JsonProperty
    public String getForsRightsUrl() {
        return forsRightsUrl;
    }

    @JsonProperty
    public void setForsRightsUrl(String forsRightsUrl) {
        this.forsRightsUrl = forsRightsUrl;
    }

    @JsonProperty
    public String getForsRightsRules() {
        return forsRightsRules;
    }

    @JsonProperty
    public void setForsRightsRules(String forsRightRules) {
        this.forsRightsRules = forsRightRules;
    }

    @JsonProperty
    public String getFormatService() {
        return formatService;
    }

    @JsonProperty
    public void setFormatService(String formatService) {
        this.formatService = formatService;
    }

    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    @JsonProperty("rawrepoOai")
    public void setDataSourceFactory(DataSourceFactory factory) {
        this.database = factory;
    }

    @JsonProperty("rawrepoOai")
    public DataSourceFactory getDataSourceFactory() {
        return database;
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
