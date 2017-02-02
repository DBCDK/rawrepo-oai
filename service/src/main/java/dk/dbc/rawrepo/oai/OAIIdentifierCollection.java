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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class OAIIdentifierCollection extends ArrayList<OAIIdentifier> {

    private static final Logger log = LoggerFactory.getLogger(OAIIdentifierCollection.class);
    private static final long serialVersionUID = -6784746125519667600L;

    private final Connection connection;
    private final Collection<String> allowedSets;

    /**
     * Make an empty collection
     *
     * @param connection  database link
     * @param allowedSets sets that are to be used is none is defined
     */
    public OAIIdentifierCollection(Connection connection, Collection<String> allowedSets) {
        this.connection = connection;
        this.allowedSets = allowedSets;
    }

    /**
     * Fetch identifiers from database
     *
     * json argument is object with:
     * {@literal
     * f: fromTimestamp*
     * u: untilTimestamp*
     * m: metadataPrefix
     * o: fromOffset (how many records with fromTimestamp has been seen)*
     * s: set*
     * }
     *
     * @param json as described
     * @param limit how many records to fetch
     * @return json as described or null if no more records
     */
    public JsonObject fetch(JsonObject json, int limit) {
        StringBuilder sb = new StringBuilder();
        String set = json.getString("s", null);
        String from = json.getString("f", null);
        String until = json.getString("u", null);
        String metadataPrefix = json.getString("m", "");
        int offset = json.getInt("o", 0);
        sb.append("SELECT pid, changed, deleted FROM oairecords JOIN oairecordsets USING (pid) WHERE");
        if (set != null) {
            sb.append(" setSpec = ?");
        } else if (allowedSets.isEmpty()) {
            return null;
        } else {
            sb.append(" setSpec IN ('");
            sb.append(allowedSets.stream().sorted().collect(Collectors.joining("', '")));
            sb.append("')");
        }
        sb.append(" AND ? in (SELECT prefix FROM oaiformats)");

        if (from != null) {
            sb.append(" AND");
            if (from.contains(".")) {
                sb.append(" DATE_TRUNC('milliseconds', changed) >= DATE_TRUNC('milliseconds', ?::timestamp)");
            } else if (from.contains("T")) {
                sb.append(" DATE_TRUNC('second', changed) >= DATE_TRUNC('second', ?::timestamp)");
            } else {
                sb.append(" DATE_TRUNC('day', changed) >= DATE_TRUNC('day', ?::timestamp)");
            }
        }
        if (until != null) {
            sb.append(" AND");
            if (until.contains(".")) {
                sb.append(" DATE_TRUNC('milliseconds', changed) <= DATE_TRUNC('milliseconds', ?::timestamp)");
            } else if (until.contains("T")) {
                sb.append(" DATE_TRUNC('second', changed) <= DATE_TRUNC('second', ?::timestamp)");
            } else {
                sb.append(" DATE_TRUNC('day', changed) <= DATE_TRUNC('day', ?::timestamp)");
            }
        }
        sb.append(" ORDER BY changed, pid OFFSET ? LIMIT ?");
        String query = sb.toString();
        log.debug("query = " + query);
        Timestamp last = null;
        int row = 0;
        try (final PreparedStatement stmt = connection.prepareStatement(query) ; final PreparedStatement sets = connection.prepareStatement("SELECT setSpec FROM oairecordsets WHERE pid = ? ORDER BY setSpec")) {
            int i = 1;
            if (set != null) {
                stmt.setString(i++, set);
            }
            stmt.setString(i++, metadataPrefix);
            if (from != null) {
                stmt.setString(i++, from);
            }
            if (until != null) {
                stmt.setString(i++, until);
            }
            stmt.setInt(i++, offset);
            stmt.setInt(i++, limit + 1);
            try (final ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    row++;
                    String pid = resultSet.getString(1);
                    Timestamp changed = resultSet.getTimestamp(2);
                    Boolean deleted = resultSet.getBoolean(3);
                    if (row <= limit) {
                        OAIIdentifier oaiRecord = new OAIIdentifier(pid, changed, deleted);
                        add(oaiRecord);
                        if (!deleted) {
                            sets.setString(1, pid);
                            try (final ResultSet setsResult = sets.executeQuery()) {
                                while (setsResult.next()) {
                                    oaiRecord.add(setsResult.getString(1));
                                }
                            }
                        }
                        if (changed.equals(last)) {
                            offset++;
                        } else {
                            last = changed;
                            offset = 1;
                        }
                    } else {
                        JsonObjectBuilder ob = Json.createObjectBuilder();
                        String continueFrom = changed.toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_INSTANT);
                        ob.add("f", continueFrom);
                        if (changed.equals(last)) {
                            ob.add("o", offset);
                        }
                        if (until != null) {
                            ob.add("u", until);
                        }
                        if (set != null) {
                            ob.add("s", set);
                        }
                        if (metadataPrefix != null) {
                            ob.add("m", metadataPrefix);
                        }
                        return ob.build();
                    }
                }
            }
        } catch (SQLException ex) {
            log.error("Exception: " + ex.getMessage());
            log.debug("Exception:", ex);
            throw new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR, "Internal Error");
        }
        return null;
    }
}