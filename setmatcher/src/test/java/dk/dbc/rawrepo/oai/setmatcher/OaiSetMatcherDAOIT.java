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
package dk.dbc.rawrepo.oai.setmatcher;

import dk.dbc.commons.testutils.postgres.connection.PostgresITConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class OaiSetMatcherDAOIT {
    private PostgresITConnection pg;
    
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
    public void testUpdateRecord() throws SQLException {
        OaiSetMatcherDAO dao = new OaiSetMatcherDAO(pg.getConnection());
        
        assertEquals(0, countRows("oairecords", pg.getConnection()));
        dao.updateRecord("870970:bibrecid", true);
        assertEquals(1, countRows("oairecords", pg.getConnection()));
        
        RecordRow recordCreated = getRecord("870970:bibrecid", pg.getConnection());
        assertEquals("870970:bibrecid", recordCreated.pid);
        assertTrue(recordCreated.deleted);
                
        dao.updateRecord("870970:bibrecid", false);
        RecordRow recordUpdated = getRecord("870970:bibrecid", pg.getConnection());
        assertEquals("870970:bibrecid", recordUpdated.pid);
        assertFalse(recordUpdated.deleted);
        assertTrue(recordUpdated.changed.getTime() > recordCreated.changed.getTime());
    }
    
    @Test
    public void testUpdateSet() throws SQLException {
        
        String PID = "870970:bibrecid";
        String NAT = "NAT";
        String BKM = "BKM";
        
        OaiSetMatcherDAO dao = new OaiSetMatcherDAO(pg.getConnection());
        dao.updateRecord(PID, true);
        
        assertEquals(0, countRows("oairecordsets", pg.getConnection()));
        
        dao.updateSet(PID, NAT, false);
        dao.updateSet(PID, BKM, false);
        
        Map<String, Set> sets = getSets(PID, pg.getConnection());
        assertFalse(sets.get(NAT.toLowerCase()).gone);
        assertFalse(sets.get(BKM.toLowerCase()).gone);
        
        dao.updateSet(PID, BKM, true);
        
        sets = getSets(PID, pg.getConnection());
        assertFalse(sets.get(NAT.toLowerCase()).gone);
        assertTrue(sets.get(BKM.toLowerCase()).gone);
        
    }
    
    
    
    private static int countRows(String table, Connection c) throws SQLException {
        try (PreparedStatement stmt = c.prepareStatement("SELECT COUNT(*) FROM " + table)) {
            ResultSet res = stmt.executeQuery();
            res.next();
            return res.getInt(1);
        }
    }
    
    
    private RecordRow getRecord(String pid, Connection c) throws SQLException{
        try (PreparedStatement stmt = c.prepareStatement("SELECT pid, changed, deleted FROM oairecords WHERE pid = ?")) {
            stmt.setString(1, pid);
            ResultSet res = stmt.executeQuery();
            res.next();
            RecordRow r = new RecordRow();         
            r.pid = res.getString(1);
            r.changed = new Date(res.getTimestamp(2).getTime());
            r.deleted = res.getBoolean(3);
            return r;
        }
    }
    
    private Map<String, Set> getSets(String pid, Connection c) throws SQLException{
        try (PreparedStatement stmt = c.prepareStatement("SELECT setSpec, gone FROM oairecordsets WHERE pid = ?")) {
            HashMap<String, Set> result = new HashMap<>();
            stmt.setString(1, pid);
            ResultSet res = stmt.executeQuery();
            while(res.next()) {
                Set s = new Set();
                s.setSpec = res.getString(1);
                s.gone = res.getBoolean(2);
                result.put(s.setSpec, s);
            }
            return result;
        }
    } 
    
    private static class Set {
        public String setSpec;
        public boolean gone;
    }
    
    private static class RecordRow {
        public String pid;
        public Date changed;
        public boolean deleted;
    }
    
}
