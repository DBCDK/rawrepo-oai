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
package dk.dbc;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.request.logging.RequestLogFactory;
import io.dropwizard.server.DefaultServerFactory;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Slf4jRequestLog;
import org.eclipse.jetty.server.handler.RequestLogHandler;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
@JsonTypeName("dbc")
public class DbcServerFactory extends DefaultServerFactory {

    private static final RequestLogFactory REQUEST_LOG_FACTORY = new RequestLogFactory() {

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public RequestLog build(String string) {
            return new Slf4jRequestLog();
        }
    };

    @Override
    protected Handler addRequestLog(Server server, Handler handler, String name) {
        if (REQUEST_LOG_FACTORY.isEnabled()) {
            final RequestLogHandler requestLogHandler = new RequestLogHandler();
            requestLogHandler.setRequestLog(REQUEST_LOG_FACTORY.build(name));
            server.addBean(requestLogHandler.getRequestLog(), true);
            requestLogHandler.setHandler(handler);
            return requestLogHandler;
        }
        return handler;
    }

}
