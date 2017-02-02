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

import dk.dbc.eeconfig.EEConfig;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.inject.Inject;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.sql.DataSource;
import javax.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
@Singleton
@Startup
@Lock(LockType.READ)
public class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);

    @Resource(lookup = C.DATASOURCE)
    private DataSource rawrepo;

    @PostConstruct
    public void init() {
        log.info("initializing config");
    }

    @Inject
    @EEConfig.Name(C.REPOSITORY_NAME)
    @EEConfig.Default(C.REPOSITORY_NAME_DEFAULT)
    private String repositoryName;

    @Inject
    @EEConfig.Name(C.BASE_URL)
    @EEConfig.Default(C.BASE_URL_DEFAULT)
    private String baseUrl;

    @Inject
    @EEConfig.Name(C.RECORDS_PR_REQUEST)
    @EEConfig.Default(C.RECORDS_PR_REQUEST_DEFAULT)
    @Min(1)
    private int recordsPrRequest;

    @Inject
    @EEConfig.Name(C.IDENTIFIERS_PR_REQUEST)
    @EEConfig.Default(C.IDENTIFIERS_PR_REQUEST_DEFAULT)
    @Min(1)
    private int identifiersPrRequest;

    @Inject
    @EEConfig.Name(C.TOKEN_MAX_AGE)
    @EEConfig.Default(C.TOKEN_MAX_AGE_DEFAULT)
    @Min(1)
    private int tokenMaxAge;

    @Inject
    @EEConfig.Name(C.FETCH_RECORD_TIMEOUT)
    @EEConfig.Default(C.FETCH_RECORD_TIMEOUT_DEFAULT)
    @Min(1)
    private long fetchRecordTimeout;

    @Inject
    @EEConfig.Name(C.NO_THROTTLE)
    @EEConfig.Default(C.NO_THROTTLE_DEFAULT)
    private boolean noThrottle;

    @Inject
    @EEConfig.Name(C.NO_AUTHENTICATION)
    @EEConfig.Default(C.NO_AUTHENTICATION_DEFAULT)
    private boolean noAuthentication;

    @Inject
    @EEConfig.Name(C.X_FORWARDED_FOR)
    @EEConfig.Default(C.X_FORWARDED_FOR_DEFAULT)
    private String xForwardedFor;

    public static Logger getLog() {
        return log;
    }

    public DataSource getRawrepo() {
        return rawrepo;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getRecordsPrRequest() {
        return recordsPrRequest;
    }

    public int getIdentifiersPrRequest() {
        return identifiersPrRequest;
    }

    public int getTokenMaxAge() {
        return tokenMaxAge;
    }

    public long getFetchRecordTimeout() {
        return fetchRecordTimeout;
    }

    public boolean isNoThrottle() {
        return noThrottle;
    }

    public boolean isNoAuthentication() {
        return noAuthentication;
    }

    public String getxForwardedFor() {
        return xForwardedFor;
    }
}
