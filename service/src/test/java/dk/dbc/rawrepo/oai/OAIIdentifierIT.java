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

import dk.dbc.commons.testutils.postgres.connection.PostgresITConnection;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
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

    private void loadRecordsFrom(String... jsons) throws SQLException {
        Connection connection = pg.getConnection();
        connection.prepareStatement("SET TIMEZONE TO 'UTC'").execute();
        try (PreparedStatement set = connection.prepareStatement("INSERT INTO oaisets (setSpec, setName) VALUES(?, ?)") ;
             PreparedStatement rec = connection.prepareStatement("INSERT INTO oairecords (pid, changed, deleted) VALUES(?, ?::timestamp, ?)") ;
             PreparedStatement recSet = connection.prepareStatement("INSERT INTO oairecordsets (pid, setSpec) VALUES(?, ?)")) {

            for (String json : jsons) {
                InputStream is = getClass().getClassLoader().getResourceAsStream(json);
                if (is == null) {
                    throw new RuntimeException("Cannot find: " + json);
                }
                JsonArray array = Json.createReader(is).readArray();
                for (Iterator<JsonValue> iterator = array.iterator() ; iterator.hasNext() ;) {
                    JsonValue next = iterator.next();
                    if (!( next instanceof JsonObject )) {
                        throw new RuntimeException("json contains: " + next + " expected an object ({...})");
                    }
                    JsonObject obj = (JsonObject) next;
                    rec.setString(1, obj.getString("pid"));
                    rec.setString(2, obj.getString("changed", DateTimeFormatter.ISO_INSTANT.format(Instant.now().atZone(ZoneId.systemDefault()))));
                    rec.setBoolean(3, obj.getBoolean("deleted", false));
                    rec.executeUpdate();
                    recSet.setString(1, obj.getString("pid"));
                    for (Iterator<JsonValue> setIterator = obj.getJsonArray("sets").iterator() ; setIterator.hasNext() ;) {
                        recSet.setString(2, ( (JsonString) setIterator.next() ).getString());
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
