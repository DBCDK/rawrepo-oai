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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class DBHealthCheck extends HealthCheck {

    private static final Logger log = LoggerFactory.getLogger(DBHealthCheck.class);

    private final DataSource datasource;

    public DBHealthCheck(DataSource datasource) {
        this.datasource = datasource;
    }

    @Override
    protected Result check() throws Exception {
        try (Connection connection = datasource.getConnection() ;
             PreparedStatement stmt = connection.prepareStatement("SELECT 1") ;
             ResultSet resultSet = stmt.executeQuery()) {
            if (resultSet.next()) {
                return Result.healthy();
            } else {
                return Result.unhealthy("Could not fetch");
            }
        } catch (SQLException ex) {
            log.error("Exception: " + ex.getMessage());
            log.debug("Exception:", ex);
            return Result.unhealthy(ex.getMessage());
        }
    }

}
