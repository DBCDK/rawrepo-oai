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
package dk.dbc.rawrepo.oai.setmatcher;

import dk.dbc.DbcConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.db.DataSourceFactory;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class OaiSetMatcherConfiguration extends DbcConfiguration {
    
    @Valid
    @NotNull
    private DataSourceFactory rawrepo;
    
    @Valid
    @NotNull
    private DataSourceFactory rawrepoOai;
    
    @NotNull
    private Integer poolSize;

    @JsonProperty("rawrepo")
    public void setRawRepoDataSourceFactory(DataSourceFactory factory) {
        this.rawrepo = factory;
    }

    @JsonProperty("rawrepo")
    public DataSourceFactory getRawRepoDataSourceFactory() {
        return rawrepo;
    }
    
    @JsonProperty("rawrepo-oai")
    public void setRawRepoOaiDataSourceFactory(DataSourceFactory factory) {
        this.rawrepoOai = factory;
    }

    @JsonProperty("rawrepo-oai")
    public DataSourceFactory getRawRepoOaiDataSourceFactory() {
        return rawrepoOai;
    }

    @JsonProperty("poolSize")
    public Integer getPoolSize() {
        return poolSize;
    }

    @JsonProperty("poolSize")
    public void setPoolSize(Integer poolSize) {
        this.poolSize = poolSize;
    }
    
}
