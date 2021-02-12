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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class OAIIdentifierIT {

    protected PostgresITConnection pg;

    public OAIIdentifierIT() throws SQLException {
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
    public void testAllowed() throws Exception {
        loadRecordsFrom("recordset_1.json");
        Connection connection = pg.getConnection();

        OAIIdentifier pid2 = OAIIdentifier.fromDb(connection, "pid:2", Arrays.asList("bkm"));
        System.out.println("pid2 = " + pid2);

    }

    @Test(expected = OAIException.class)
    public void testNotAllowed() throws Exception {
        loadRecordsFrom("recordset_1.json");
        Connection connection = pg.getConnection();

        OAIIdentifier pid1 = OAIIdentifier.fromDb(connection, "pid:1", Arrays.asList("bkm"));
        System.out.println("pid1 = " + pid1);
    }

    @Test
    public void testOnlySetsShownThatAreAllowed() throws Exception {
        loadRecordsFrom("recordset_1.json");
        Connection connection = pg.getConnection();

        OAIIdentifier pid2 = OAIIdentifier.fromDb(connection, "pid:2", Arrays.asList("nat"));
        System.out.println("pid2 = " + pid2);
        assertFalse(pid2.contains("bkm"));
    }

    @Test
    public void testNotSelectGone() throws Exception {
        loadRecordsFrom("recordset_1.json");
        Connection connection = pg.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE oairecordsets SET gone=TRUE WHERE pid='pid:2' AND setSpec='bkm'")) {
            stmt.executeUpdate();
        }

        OAIIdentifier pid2 = OAIIdentifier.fromDb(connection, "pid:2", Arrays.asList("nat", "bkm"));
        System.out.println("pid2 = " + pid2);
        assertFalse(pid2.contains("bkm"));
    }

    @Test
    public void testAllGoneIsDeleted() throws Exception {
        loadRecordsFrom("recordset_1.json");
        Connection connection = pg.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE oairecordsets SET gone=TRUE WHERE pid='pid:2' AND setSpec='nat'")) {
            stmt.executeUpdate();
        }

        OAIIdentifier pid2 = OAIIdentifier.fromDb(connection, "pid:2", Arrays.asList("nat"));
        System.out.println("pid2 = " + pid2);
        assertTrue(pid2.isEmpty());
        assertTrue(pid2.isDeleted());
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
