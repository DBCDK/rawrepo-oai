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


import com.codahale.metrics.Counter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
@Singleton
@Startup
public class MetricsRegistry {

    private final MetricRegistry metrics = new MetricRegistry();

    private JmxReporter reporter;

    @PostConstruct
    public void create() {
        reporter = JmxReporter.forRegistry(metrics).build();
        reporter.start();
    }

    @PreDestroy
    public void destroy() {
        if (reporter != null) {
            reporter.stop();
        }
    }

    @Produces
    public Timer makeTimer(InjectionPoint ip) {
        Class<?> clazz = ip.getMember().getDeclaringClass();
        String name = ip.getMember().getName();
        if (name.endsWith("Timer")) {
            name = name.substring(0, name.length() - "Timer".length());
        }
        return metrics.timer(MetricRegistry.name(clazz, name));
    }

    @Produces
    public Counter makeCounter(InjectionPoint ip) {
        Class<?> clazz = ip.getMember().getDeclaringClass();
        String name = ip.getMember().getName();
        if (name.endsWith("Counter")) {
            name = name.substring(0, name.length() - "Counter".length());
        }
        return metrics.counter(MetricRegistry.name(clazz, name));
    }

}
