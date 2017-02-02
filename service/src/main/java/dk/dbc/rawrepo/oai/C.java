/*
 * dbc-rawrepo-maintain
 * Copyright (C) 2015 Dansk Bibliotekscenter a/s, Tempovej 7-11, DK-2750 Ballerup,
 * Denmark. CVR: 15149043*
 *
 * This file is part of dbc-rawrepo-maintain.
 *
 * dbc-rawrepo-maintain is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * dbc-rawrepo-maintain is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with dbc-rawrepo-maintain.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.rawrepo.oai;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class C {

    public static final String DATASOURCE = "jdbc/rawrepo-oai/rawrepo-oai";
    public static final String PROPERTIES = "rawrepo-oai";

    public static final String REPOSITORY_NAME = "repository-name";
    public static final String REPOSITORY_NAME_DEFAULT = "DBC OAI";

    public static final String BASE_URL = "base-url";
    public static final String BASE_URL_DEFAULT = "http://dbc.dk/";

    public static final String RECORDS_PR_REQUEST = "records-pr-request";
    public static final String RECORDS_PR_REQUEST_DEFAULT = "10";

    public static final String IDENTIFIERS_PR_REQUEST = "identifiers-pr-request";
    public static final String IDENTIFIERS_PR_REQUEST_DEFAULT = "50";

    public static final String TOKEN_MAX_AGE = "token-max-age";
    public static final String TOKEN_MAX_AGE_DEFAULT = "8";

    public static final String FETCH_RECORD_TIMEOUT = "fetch-record-timeout";
    public static final String FETCH_RECORD_TIMEOUT_DEFAULT = "30";

    public static final String NO_THROTTLE = "no-throttle";
    public static final String NO_THROTTLE_DEFAULT = "false";

    public static final String NO_AUTHENTICATION = "no-authentication";
    public static final String NO_AUTHENTICATION_DEFAULT = "false";

    public static final String X_FORWARDED_FOR = "x-forwarded-for";
    public static final String X_FORWARDED_FOR_DEFAULT = "127.0.0.1/32 172.16.0.0/12";

    public static final String FORS_RIGHTS_URL = "fors-rights-url";
    public static final String FORS_RIGHTS_URL_DEFAULT = "http://forsrights.addi.dk/1.2/";

    public static final String FORS_RIGHTS_RULES = "fors-rights-rules";
    public static final String FORS_RIGHTS_RULES_DEFAULT = "netpunkt.dk,500=bkm";

}
