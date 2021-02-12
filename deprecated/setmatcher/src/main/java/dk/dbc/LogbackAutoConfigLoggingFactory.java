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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.dropwizard.logging.LoggingFactory;
import io.dropwizard.logging.LoggingUtil;

/**
 * https://github.com/dropwizard/dropwizard/issues/1567 Override
 * getLoggingFactory for your configuration
 */
public class LogbackAutoConfigLoggingFactory implements LoggingFactory {

    @JsonIgnore
    private final LoggerContext loggerContext;

    @JsonIgnore
    private final ContextInitializer contextInitializer;

    public LogbackAutoConfigLoggingFactory() {
        this.loggerContext = LoggingUtil.getLoggerContext();
        this.contextInitializer = new ContextInitializer(loggerContext);
    }

    @Override
    public void configure(MetricRegistry metricRegistry, String name) {
        reset();
    }

    @Override
    public void stop() {
        loggerContext.stop();
    }

    @Override
    public void reset() throws RuntimeException {
        try {
            loggerContext.reset();
            contextInitializer.autoConfig();
        } catch (JoranException e) {
            throw new RuntimeException(e);
        }
    }
}
