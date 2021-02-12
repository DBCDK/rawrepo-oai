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
package dk.dbc.rawrepo.oai.formatter.healthchecks;

import com.codahale.metrics.health.HealthCheck;
import dk.dbc.rawrepo.oai.formatter.javascript.JavascriptWorkerPool;
import dk.dbc.rawrepo.oai.formatter.javascript.JavascriptWorkerPool.JavaScriptWorker;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class JavaScriptHealthCheck extends HealthCheck {

    private static final Logger log = LoggerFactory.getLogger(JavaScriptHealthCheck.class);
    
    private final JavascriptWorkerPool pool;

    public JavaScriptHealthCheck(JavascriptWorkerPool pool) {
        this.pool = pool;
    }

    @Override
    protected Result check() {
        try(JavaScriptWorker worker = pool.borrowWorker()) {
            HashSet<String> allowedFormats = worker.getAllowedFormats();
            if(!allowedFormats.isEmpty()) {
                return Result.healthy();
            }
            return Result.unhealthy("No allowed formats");
        } catch (Exception ex) {
            log.error("Unhealthy JavaScript pool", ex);
            return Result.unhealthy(ex.getMessage());
        }
    }
}
