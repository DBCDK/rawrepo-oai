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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private transient final Connection connection;
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
     * json argument is object with: null null null null null     {@literal
     * f: fromTimestamp*
     * u: untilTimestamp*
     * m: metadataPrefix
     * o: fromOffset (how many records with fromTimestamp has been seen)*
     * s: set*
     * }
     *
     * @param json  as described
     * @param limit how many records to fetch
     * @return json as described or null if no more records
     */
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public ObjectNode fetch(ObjectNode json, int limit) {
        log.debug("limit = " + limit);
        StringBuilder sb = new StringBuilder();
        String set = json.path("s").asText(null);
        String from = json.path("f").asText(null);
        String until = json.path("u").asText(null);
        String metadataPrefix = json.path("m").asText("");
        int offset = json.path("o").asInt(0);
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
                sb.append(" DATE_TRUNC('milliseconds', changed) >= DATE_TRUNC('milliseconds', ?::timestamp AT TIME ZONE 'UTC')");
            } else if (from.contains("T")) {
                sb.append(" DATE_TRUNC('second', changed) >= DATE_TRUNC('second', ?::timestamp AT TIME ZONE 'UTC')");
            } else {
                sb.append(" DATE_TRUNC('day', changed) >= DATE_TRUNC('day', ?::timestamp AT TIME ZONE 'UTC')");
            }
        }
        if (until != null) {
            sb.append(" AND");
            if (until.contains(".")) {
                sb.append(" DATE_TRUNC('milliseconds', changed) <= DATE_TRUNC('milliseconds', ?::timestamp AT TIME ZONE 'UTC')");
            } else if (until.contains("T")) {
                sb.append(" DATE_TRUNC('second', changed) <= DATE_TRUNC('second', ?::timestamp AT TIME ZONE 'UTC')");
            } else {
                sb.append(" DATE_TRUNC('day', changed) <= DATE_TRUNC('day', ?::timestamp AT TIME ZONE 'UTC')");
            }
        }
        sb.append(" GROUP BY pid");
        if (set != null) {
            sb.append(", gone");
        }
        sb.append(" ORDER BY changed, pid OFFSET ? LIMIT ?");
        String query = sb.toString();
        log.debug("query = " + query);

        sb = new StringBuilder();
        sb.append("SELECT setSpec FROM oairecordsets WHERE pid = ? AND setSpec IN ('")
                .append(allowedSets.stream().sorted().collect(Collectors.joining("', '")))
                .append("') AND NOT gone ORDER BY setSpec");
        String setQuery = sb.toString();

        Timestamp last = null;
        int row = 0;
        try (PreparedStatement stmt = connection.prepareStatement(query) ;
             PreparedStatement sets = connection.prepareStatement(setQuery)) {
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
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    row++;
                    String pid = resultSet.getString(1);
                    Timestamp changed = resultSet.getTimestamp(2);
                    Boolean deleted = resultSet.getBoolean(3);
                    if (row <= limit) {
                        OAIIdentifier oaiRecord = new OAIIdentifier(pid, changed, deleted);
                        sets.setString(1, pid);
                        try (ResultSet setsResult = sets.executeQuery()) {
                            while (setsResult.next()) {
                                oaiRecord.add(setsResult.getString(1));
                            }
                        }
                        if (oaiRecord.isEmpty() && !oaiRecord.isDeleted()) {
                            oaiRecord = new OAIIdentifier(pid, changed, true);
                        }
                        add(oaiRecord);
                        if (changed.equals(last)) {
                            offset++;
                        } else {
                            last = changed;
                            offset = 1;
                        }
                    } else {
                        ObjectNode obj = OBJECT_MAPPER.createObjectNode();
                        String continueFrom = changed.toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_INSTANT);
                        obj.put("f", continueFrom);
                        if (changed.equals(last)) {
                            obj.put("o", offset);
                        }
                        if (until != null) {
                            obj.put("u", until);
                        }
                        if (set != null) {
                            obj.put("s", set);
                        }
                        if (metadataPrefix != null) {
                            obj.put("m", metadataPrefix);
                        }
                        log.debug("continueFrom = " + obj);
                        return obj;
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
