/*
 * Copyright (C) 2017 DBC A/S (http://dbc.dk/)
 *
 * This is part of dbc-rawrepo-oai-harvester-dw
 *
 * dbc-rawrepo-oai-harvester-dw is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * dbc-rawrepo-oai-harvester-dw is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.rawrepo.oai.setmatcher.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class OaiSetMatcherDAO {
    private final Connection c;
    
    private static final String UPSERT_RECORD = "INSERT INTO oairecords( pid, deleted )"
                                              + " values( ?, ? )"
                                              + " ON CONFLICT ( pid )"
                                              + " DO UPDATE SET deleted = EXCLUDED.deleted, changed = EXCLUDED.changed";
    
    private static final String UPSERT_RECORD_SET = "INSERT INTO oairecordsets( pid, setSpec, gone )"
                                                  + " values( ?, ?, ? )"
                                                  + " ON CONFLICT ( pid, setSpec )"
                                                  + " DO UPDATE SET pid = EXCLUDED.pid, setSpec = EXCLUDED.setSpec, gone = EXCLUDED.gone";
    
    public OaiSetMatcherDAO(Connection c) {
        this.c = c;     
    }
    
    public void updateRecord(String pid, boolean deleted) throws SQLException {
        try (PreparedStatement stmt = c.prepareStatement(UPSERT_RECORD)) {
            stmt.setString(1, pid);
            stmt.setBoolean(2, deleted);
            int res = stmt.executeUpdate();
            if(res == 0) {
                throw new RuntimeException("Record not updated");
            }
        }
    }
    
    public void updateSet(String pid, String setName, boolean gone) throws SQLException {
        try (PreparedStatement stmt = c.prepareStatement(UPSERT_RECORD_SET)) {
            stmt.setString(1, pid);
            stmt.setString(2, setName.toLowerCase());
            stmt.setBoolean(3, gone);
            int res = stmt.executeUpdate();
            if(res == 0) {
                throw new RuntimeException("Record Set not updated");
            }
        }        
    }
    
    public RecordSet[] fetchSets(String pid) throws SQLException {
        try (PreparedStatement stmt = c.prepareStatement("SELECT setSpec, gone FROM oairecordsets WHERE pid = ?")) {        
            ArrayList<RecordSet> result = new ArrayList<>();
            
            stmt.setString(1, pid);
            ResultSet res = stmt.executeQuery();
            while(res.next()) {
                RecordSet s = new RecordSet();
                s.setSpec = res.getString(1);
                s.gone = res.getBoolean(2);
                result.add(s);
            }
            return result.toArray(new RecordSet[result.size()]);
        }
    } 
    
    public static class RecordSet {        
        public String setSpec;
        public boolean gone;
    }
}
