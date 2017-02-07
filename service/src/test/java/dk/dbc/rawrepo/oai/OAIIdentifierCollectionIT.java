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
import dk.dbc.commons.testutils.postgres.connection.PostgresITConnection;
import dk.dbc.oai.pmh.OAIPMHerrorcodeType;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class OAIIdentifierCollectionIT {

    protected PostgresITConnection pg;

    public OAIIdentifierCollectionIT() throws SQLException {
        pg = null;

    }

    @Before
    public void setUp() throws SQLException {
        pg = new PostgresITConnection("rawrepooai");
        pg.clearTables("oairecordsets", "oairecords");
    }

    @After
    public void tearDown() throws SQLException {
        pg.close();
    }

    @Test
    public void testContinue() throws Exception {
        loadRecordsFrom("recordset_1.json");
        Connection connection = pg.getConnection();

        ObjectNode json = json("'s':'nat','u':'2017-02-20','m':'marcx'");

        OAIIdentifierCollection recordCollection = new OAIIdentifierCollection(connection, Arrays.asList("nat", "bkm"));
        ObjectNode cont = recordCollection.fetch(json, 4);
        System.out.println("recordCollection = " + recordCollection);
        System.out.println("cont = " + cont);
        System.out.println("cont = " + ResumptionToken.encode(cont, 48));

        assertNotNull(cont);
        recordCollection = new OAIIdentifierCollection(connection, Arrays.asList("nat", "bkm"));
        cont = recordCollection.fetch(cont, 4);
        System.out.println("recordCollection = " + recordCollection);
        System.out.println("cont = " + cont);

        assertNull(cont);
    }

    @Test
    public void testNoneDuplicates() throws Exception {
        loadRecordsFrom("recordset_1.json");
        Connection connection = pg.getConnection();

        ObjectNode json = json("'m':'marcx'");

        OAIIdentifierCollection recordCollection = new OAIIdentifierCollection(connection, Arrays.asList("nat", "bkm"));
        recordCollection.fetch(json, 100);
        System.out.println("recordCollection = " + recordCollection);
        Set<String> uniq = recordCollection.stream()
                .map(id -> id.getIdentifier())
                .collect(Collectors.toSet());
        assertEquals(uniq.size(), recordCollection.size());
    }

    /**
     * Find a record that has both nat & bkm see that sets don't include bkm
     *
     * @throws Exception just in case
     */
    @Test
    public void testNoAccess() throws Exception {
        loadRecordsFrom("recordset_1.json");
        Connection connection = pg.getConnection();

        ObjectNode json = json("'m':'marcx'");
        OAIIdentifierCollection recordCollection = new OAIIdentifierCollection(connection, Arrays.asList("nat"));
        recordCollection.fetch(json, 100);
        OAIIdentifier id = recordCollection.stream()
                .filter(i -> "pid:2".equals(i.getIdentifier()))
                .findFirst()
                .orElseThrow(() -> new OAIException(OAIPMHerrorcodeType.NO_RECORDS_MATCH, "WHAT!"));
        System.out.println("id = " + id);
        assertEquals(1, id.size());
        assertEquals("nat", id.get(0));
    }

    @Test
    public void testGoneIsDeleted() throws Exception {
        loadRecordsFrom("recordset_1.json");
        Connection connection = pg.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement("UPDATE oairecordsets SET gone=TRUE WHERE pid='pid:2' AND setSpec='nat'")) {
            stmt.executeUpdate();
        }

        ObjectNode json = json("'s':'bkm','m':'marcx'");
        OAIIdentifierCollection recordCollection = new OAIIdentifierCollection(connection, Arrays.asList("nat"));
        recordCollection.fetch(json, 100);
        OAIIdentifier id = recordCollection.stream()
                .filter(i -> "pid:2".equals(i.getIdentifier()))
                .findFirst()
                .orElseThrow(() -> new OAIException(OAIPMHerrorcodeType.NO_RECORDS_MATCH, "WHAT!"));
        System.out.println("id = " + id);
        assertTrue(id.isEmpty());
        assertTrue(id.isDeleted());
    }

    @Test
    public void testGoneIsNotDeleted() throws Exception {
        loadRecordsFrom("recordset_1.json");
        Connection connection = pg.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement("UPDATE oairecordsets SET gone=TRUE WHERE pid='pid:2' AND setSpec='bkm'")) {
            stmt.executeUpdate();
        }

        ObjectNode json = json("'m':'marcx'");
        OAIIdentifierCollection recordCollection = new OAIIdentifierCollection(connection, Arrays.asList("nat", "bkm"));
        recordCollection.fetch(json, 100);
        OAIIdentifier id = recordCollection.stream()
                .filter(i -> "pid:2".equals(i.getIdentifier()))
                .findFirst()
                .orElseThrow(() -> new OAIException(OAIPMHerrorcodeType.NO_RECORDS_MATCH, "WHAT!"));
        System.out.println("id = " + id);
        assertFalse(id.contains("bkm"));
    }

    @Test
    public void testUnknownFormat() throws Exception {
        loadRecordsFrom("recordset_1.json");
        Connection connection = pg.getConnection();

        ObjectNode json = json("'s':'bkm','m':'???'");

        OAIIdentifierCollection recordCollection = new OAIIdentifierCollection(connection, Arrays.asList("nat", "bkm"));
        ObjectNode cont = recordCollection.fetch(json, 4);
        assertNull(cont);
        assertTrue(recordCollection.isEmpty());
    }

    private ObjectNode json(String json) throws IOException {
        return new ObjectMapper().readValue("{" + json.replaceAll("'", "\"") + "}",
                                            ObjectNode.class);
    }

    private void loadRecordsFrom(String... jsons) throws SQLException, IOException {
        Connection connection = pg.getConnection();
        connection.prepareStatement("SET TIMEZONE TO 'UTC'").execute();
        try (PreparedStatement rec = connection.prepareStatement("INSERT INTO oairecords (pid, changed, deleted) VALUES(?, ?::timestamp, ?)") ;
             PreparedStatement recSet = connection.prepareStatement("INSERT INTO oairecordsets (pid, setSpec) VALUES(?, ?)")) {

            for (String json : jsons) {
                InputStream is = getClass().getClassLoader().getResourceAsStream(json);
                if (is == null) {
                    throw new RuntimeException("Cannot find: " + json);
                }
                ObjectMapper objectMapper = new ObjectMapper();
                List<ObjectNode> array = objectMapper.readValue(is, List.class);
                for (Object object : array) {
                    Map<String,Object> obj =  (Map<String,Object>) object;
                    rec.setString(1, (String) obj.get("pid"));
                    rec.setString(2, (String) obj.getOrDefault("changed", DateTimeFormatter.ISO_INSTANT.format(Instant.now().atZone(ZoneId.systemDefault()))));
                    rec.setBoolean(3, (boolean) obj.getOrDefault("deleted", false));
                    rec.executeUpdate();
                    recSet.setString(1, (String) obj.get("pid"));
                    List<Object> sets = (List<Object>) obj.get("sets");

                    for (Object set : sets) {
                        recSet.setString(2, (String) set);
                        recSet.executeUpdate();
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

}
