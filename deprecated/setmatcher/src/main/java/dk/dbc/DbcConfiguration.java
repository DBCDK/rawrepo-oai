/*
 * Copyright (C) 2017 DBC A/S (http://dbc.dk/)
 *
 * This is part of dbc-rawrepo-oai-setmatcher-dw
 *
 * dbc-rawrepo-oai-setmatcher-dw is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * dbc-rawrepo-oai-setmatcher-dw is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc;

import io.dropwizard.Configuration;
import io.dropwizard.logging.LoggingFactory;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class DbcConfiguration extends Configuration {

    private final LogbackAutoConfigLoggingFactory logging = new LogbackAutoConfigLoggingFactory();

    @Override
    public LoggingFactory getLoggingFactory() {
        return logging;
    }

}
