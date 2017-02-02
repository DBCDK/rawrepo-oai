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

import dk.dbc.oai.pmh.HeaderType;
import dk.dbc.oai.pmh.OAIPMHerrorcodeType;
import dk.dbc.oai.pmh.ObjectFactory;
import dk.dbc.oai.pmh.RecordType;
import dk.dbc.oai.pmh.StatusType;
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
 * List of sets identifier is in combined with identifier, deleted status and
 * modified timestamp
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class OAIIdentifier extends ArrayList<String> {

    private static final Logger log = LoggerFactory.getLogger(OAIIdentifier.class);

    private static final long serialVersionUID = 1480719915786481639L;

    private static final String SELECT_RECORD_PRE = "SELECT changed, deleted FROM oairecords JOIN oairecordsets USING (pid) WHERE pid = ? AND setSpec IN ('";
    private static final String SELECT_RECORD_POST = "')";
    private static final String SELECT_SET_PRE = "SELECT setSpec FROM oairecordsets WHERE pid = ? AND setSpec IN ('";
    private static final String SELECT_SET_POST = "') ORDER BY setSpec";

    private static final ZoneId UTC = ZoneId.of("UTC");
    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    private final String identifier;
    private final String timestamp;
    private final boolean deleted;

    public OAIIdentifier(String identifier, Timestamp timestamp, boolean deleted) {
        this.identifier = identifier;
        this.deleted = deleted;
        this.timestamp = timestamp.toInstant().atZone(UTC).format(DateTimeFormatter.ISO_INSTANT);
    }

    /**
     * The object identifier (870970...basis...)
     *
     * @return Identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * When the record is modified in oai context
     *
     * @return iso instant timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Has the record been tagged deleted in the database
     *
     * @return status of record
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Construct a record (XML) object
     *
     * @return XML Object
     */
    public RecordType toRecord() {
        RecordType record = OBJECT_FACTORY.createRecordType();
        record.setHeader(toHeader());
        return record;
    }

    /**
     * Construct a header (XML) object
     *
     * @return XML Object
     */
    public HeaderType toHeader() {
        HeaderType header = OBJECT_FACTORY.createHeaderType();
        header.setIdentifier(identifier);
        if (deleted) {
            header.setStatus(StatusType.DELETED);
        }
        header.setDatestamp(timestamp);
        header.getSetSpecs().addAll(this);
        return header;
    }

    /**
     * Pull a single identifier from the database
     *
     * @param connection  database
     * @param identifier  identifier
     * @param allowedSets sets allowed in header
     * @return new OAIIdentifier
     */
    @SuppressWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public static OAIIdentifier fromDb(Connection connection, String identifier, Collection<String> allowedSets) {
        String setInline = allowedSets.stream()
                .sorted()
                .collect(Collectors.joining("', '"));
        String recordQuery = new StringBuilder()
                .append(SELECT_RECORD_PRE)
                .append(setInline)
                .append(SELECT_RECORD_POST)
                .toString();
        String setQuery = new StringBuilder()
                .append(SELECT_SET_PRE)
                .append(setInline)
                .append(SELECT_SET_POST)
                .toString();

        OAIIdentifier oaiIdentifier;
        try (PreparedStatement stmt = connection.prepareStatement(recordQuery)) {
            stmt.setString(1, identifier);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (!resultSet.next()) {
                    throw new OAIException(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, "No matching identifier or needs authentication");
                }
                oaiIdentifier = new OAIIdentifier(identifier, resultSet.getTimestamp(1), resultSet.getBoolean(2));
            }
        } catch (SQLException ex) {
            log.error("Exception: " + ex.getMessage());
            log.debug("Exception:", ex);
            throw new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR, "Internal Error");
        }
        if (!oaiIdentifier.isDeleted()) {
            try (PreparedStatement stmt = connection.prepareStatement(setQuery)) {
                stmt.setString(1, identifier);
                try (ResultSet resultSet = stmt.executeQuery()) {
                    while (resultSet.next()) {
                        oaiIdentifier.add(resultSet.getString(1));
                    }
                }
            } catch (SQLException ex) {
                log.error("Exception: " + ex.getMessage());
                log.debug("Exception:", ex);
                throw new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR, "Internal Error");
            }
        }
        return oaiIdentifier;
    }

    @Override
    public String toString() {
        return "OAIIdentifier{" + "identifier=" + identifier + "; timestamp=" + timestamp + "; deleted=" + deleted + "; sets=" + String.join(", ", this) + '}';
    }

}
