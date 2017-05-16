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
package dk.dbc.rawrepo.oai.formatter.resources;

import dk.dbc.commons.testutils.postgres.connection.PostgresITConnection;
import dk.dbc.marcxmerge.MarcXChangeMimeType;
import dk.dbc.rawrepo.RawRepoDAO;
import dk.dbc.rawrepo.RawRepoException;
import dk.dbc.rawrepo.Record;
import dk.dbc.rawrepo.RecordId;
import dk.dbc.rawrepo.oai.formatter.javascript.MarcXChangeWrapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/*
 * @author DBC {@literal <dbc.dk>}
 */
public class OaiFormatterResourceIT {
    
    private Connection connection;
    private PostgresITConnection postgres;

    @Before
    public void setup() throws SQLException, ClassNotFoundException {
        postgres = new PostgresITConnection("rawrepo");        
        postgres.clearTables("relations", "records");
        connection = postgres.getConnection();
        connection.setAutoCommit(false);
        connection.prepareStatement("SET log_statement = 'all';").execute();        
    }
    
    @After
    public void after() throws SQLException {
        connection.rollback();
        postgres.close();
        connection.close();
    }
    
    public OaiFormatterResourceIT() {
    }

    @Test
    public void testFetchRecordCollection_withHeadSectionVolume() throws Exception {
                
        RawRepoDAO dao = RawRepoDAO.builder(connection).build();
        
        Record head = createRecord("bib1", dao);
        Record section1 = createRecord("bib2", dao);
        Record section2 = createRecord("bib3", dao);
        Record volume1 = createRecord("bib4", dao);
        Record volume2 = createRecord("bib5", dao);

        setChild(head, section1, dao);
        setChild(head, section2, dao);
        setChild(section1, volume1, dao);
        setChild(section1, volume2, dao);
        
        MarcXChangeWrapper[] collection = OaiFormatterResource.fetchRecordCollection(
                volume1.getId().getAgencyId(), 
                volume1.getId().getBibliographicRecordId(), dao);
                               
        assertEquals(3, collection.length);        
        assertEquals("content_bib4", collection[0].content);
        assertEquals("content_bib2", collection[1].content);
        assertEquals("content_bib1", collection[2].content);        

        assertEquals(0, collection[0].children.length);
        
        assertEquals(new HashSet<>(Arrays.asList(
                new MarcXChangeWrapper.Record(volume1.getId().getBibliographicRecordId(), 
                        volume1.getId().getAgencyId()),
                new MarcXChangeWrapper.Record(volume2.getId().getBibliographicRecordId(), 
                        volume2.getId().getAgencyId()))), 
                new HashSet<>(Arrays.asList(collection[1].children)));
        
        assertEquals(new HashSet<>(Arrays.asList(
                new MarcXChangeWrapper.Record(section1.getId().getBibliographicRecordId(), 
                        section1.getId().getAgencyId()),
                new MarcXChangeWrapper.Record(section2.getId().getBibliographicRecordId(), 
                        section2.getId().getAgencyId()))), 
                new HashSet<>(Arrays.asList(collection[2].children)));
    }
    
    @Test
    public void testFetchRecordCollection_withHeadVolume() throws Exception {
                
        RawRepoDAO dao = RawRepoDAO.builder(connection).build();
        
        Record head = createRecord("bib1", dao);
        Record volume1 = createRecord("bib4", dao);
        Record volume2 = createRecord("bib5", dao);

        setChild(head, volume1, dao);
        setChild(head, volume2, dao);
        
        MarcXChangeWrapper[] collection = OaiFormatterResource.fetchRecordCollection(
                volume1.getId().getAgencyId(), 
                volume1.getId().getBibliographicRecordId(), dao);
                               
        assertEquals(2, collection.length);        
        assertEquals("content_bib4", collection[0].content);
        assertEquals("content_bib1", collection[1].content);

        assertEquals(0, collection[0].children.length);
        
        assertEquals(new HashSet<>(Arrays.asList(
                new MarcXChangeWrapper.Record(volume1.getId().getBibliographicRecordId(), 
                        volume1.getId().getAgencyId()),
                new MarcXChangeWrapper.Record(volume2.getId().getBibliographicRecordId(), 
                        volume2.getId().getAgencyId()))), 
                new HashSet<>(Arrays.asList(collection[1].children)));

    }
    
    void setChild(Record head, Record child, RawRepoDAO dao) throws RawRepoException{
        Set<RecordId> relationsFrom = dao.getRelationsFrom(child.getId());
        relationsFrom.add(head.getId());
        dao.setRelationsFrom(child.getId(), relationsFrom);
    }
    
    Record createRecord(String bibId, RawRepoDAO dao) throws RawRepoException {
        RecordId id = new RecordId(bibId, 870970);
        Record record = dao.fetchRecord(id.getBibliographicRecordId(), id.getAgencyId());
        record.setMimeType(MarcXChangeMimeType.MARCXCHANGE);
        record.setContent(("content_" + bibId).getBytes());
        dao.saveRecord(record);
        return record;
    }
    
}
