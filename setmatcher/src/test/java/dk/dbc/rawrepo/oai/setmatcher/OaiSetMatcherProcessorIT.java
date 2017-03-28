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

import com.codahale.metrics.MetricRegistry;
import dk.dbc.commons.testutils.postgres.connection.PostgresITConnection;
import dk.dbc.rawrepo.QueueJob;
import dk.dbc.rawrepo.RawRepoDAO;
import dk.dbc.rawrepo.RawRepoException;
import dk.dbc.rawrepo.Record;
import dk.dbc.rawrepo.RecordId;
import dk.dbc.rawrepo.oai.setmatcher.OaiSetMatcherDAO.RecordSet;
import java.sql.SQLException;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class OaiSetMatcherProcessorIT {
    
    private final String BIB_REC_ID = "bibrecid";
    private final int AGENCY_ID = 870970;
    private final byte[] RECORD_CONTENT = new byte[]{0};
    private PostgresITConnection rawrepo;
    private PostgresITConnection rawrepoOai;
    private RawRepoDAO rawrepoDao;
    private OaiSetMatcherDAO rawrepoOaiDao;
    private Record record;
    private QueueJob queueJob;
            
    @Before
    public void setUp() throws SQLException, RawRepoException {
        rawrepo = new PostgresITConnection("rawrepo");
        rawrepo.clearTables("records");
        
        rawrepoOai = new PostgresITConnection("rawrepooai");
        rawrepoOai.clearTables("oairecordsets", "oairecords");
        
        rawrepoDao = RawRepoDAO.builder(rawrepo.getConnection()).build();
        
        record = rawrepoDao.fetchRecord(BIB_REC_ID, AGENCY_ID);
        record.setContent(RECORD_CONTENT);
        record.setMimeType("some");
        rawrepoDao.saveRecord(record);
        
        rawrepoOaiDao = new OaiSetMatcherDAO(rawrepoOai.getConnection());
        
        queueJob = createQueueJob(BIB_REC_ID, AGENCY_ID);
    }
    
    private OaiSetMatcherProcessor createProcessor(JavaScriptWorker jsWorker){
        OaiSetMatcherProcessor setMatcher = new OaiSetMatcherProcessor(null, null, jsWorker, null, new MetricRegistry());
        setMatcher.rawrepoConnection = rawrepo.getConnection();
        setMatcher.rawrepoOAIConnection = rawrepoOai.getConnection();
        return setMatcher;
    }

    @After
    public void tearDown() throws SQLException {
        rawrepo.close();
        rawrepoOai.close();
    }
    
    @Test
    public void testProcessQueueJob_partOfBKM() throws Exception {
        
        JavaScriptWorker jsWorker = mock(JavaScriptWorker.class);
        when(jsWorker.getOaiSets(eq(AGENCY_ID), eq(new String(RECORD_CONTENT, "UTF-8")))).thenReturn(new String[]{"BKM"}); 

        OaiSetMatcherProcessor processor = createProcessor(jsWorker);
        processor.process(queueJob);
        
        RecordSet[] sets = rawrepoOaiDao.fetchSets(AGENCY_ID + ":" + BIB_REC_ID);
        assertEquals(1, sets.length);
        
        assertEquals("bkm", sets[0].setSpec);
        assertEquals(false, sets[0].gone);
    }
    
    @Test
    public void testProcessQueueJob_partOfNAT() throws Exception {
        
        JavaScriptWorker jsWorker = mock(JavaScriptWorker.class);
        when(jsWorker.getOaiSets(eq(AGENCY_ID), eq(new String(RECORD_CONTENT, "UTF-8")))).thenReturn(new String[]{"NAT"}); 
        
        OaiSetMatcherProcessor processor = createProcessor(jsWorker);
        processor.process(queueJob);
                
        RecordSet[] sets = rawrepoOaiDao.fetchSets(AGENCY_ID + ":" + BIB_REC_ID);
        assertEquals(1, sets.length);
        
        assertEquals("nat", sets[0].setSpec);
        assertEquals(false, sets[0].gone);
    }
    
    @Test
    public void testProcessQueueJob_partOfNATandBKM() throws Exception {
        
        JavaScriptWorker jsWorker = mock(JavaScriptWorker.class);
        when(jsWorker.getOaiSets(eq(AGENCY_ID), eq(new String(RECORD_CONTENT, "UTF-8")))).thenReturn(new String[]{"NAT", "BKM"}); 

        OaiSetMatcherProcessor processor = createProcessor(jsWorker);
        processor.process(queueJob);
        
        RecordSet[] sets = rawrepoOaiDao.fetchSets(AGENCY_ID + ":" + BIB_REC_ID);
        assertEquals(2, sets.length);
        
        assertEquals("bkm", sets[0].setSpec);
        assertEquals(false, sets[0].gone);
        
        assertEquals("nat", sets[1].setSpec);
        assertEquals(false, sets[1].gone);
    }
    
    
    @Test
    public void testProcessQueueJob_whenRecordDeleted_goneFromAllSets() throws Exception {
        
        JavaScriptWorker jsWorker = mock(JavaScriptWorker.class);
        when(jsWorker.getOaiSets(eq(AGENCY_ID), eq(new String(RECORD_CONTENT, "UTF-8")))).thenReturn(new String[]{"NAT", "BKM"}); 

        OaiSetMatcherProcessor processor = createProcessor(jsWorker);
        processor.process(queueJob);
                
        RecordSet[] sets = rawrepoOaiDao.fetchSets(AGENCY_ID + ":" + BIB_REC_ID);
        assertEquals(2, sets.length);
        
        assertEquals("bkm", sets[0].setSpec);
        assertEquals(false, sets[0].gone);
        
        assertEquals("nat", sets[1].setSpec);
        assertEquals(false, sets[1].gone);
        
        record.setDeleted(true);
        rawrepoDao.saveRecord(record);
        
        processor.process(queueJob);
        
        sets = rawrepoOaiDao.fetchSets(AGENCY_ID + ":" + BIB_REC_ID);
        assertEquals(2, sets.length);
        
        assertEquals("bkm", sets[0].setSpec);
        assertEquals(true, sets[0].gone);
        
        assertEquals("nat", sets[1].setSpec);
        assertEquals(true, sets[1].gone);
    }
    
    @Test
    public void testProcessQueueJob_whenRecordGoneFromSet_itIsGone() throws Exception {
        
        JavaScriptWorker jsWorker = mock(JavaScriptWorker.class);
        when(jsWorker.getOaiSets(eq(AGENCY_ID), eq(new String(RECORD_CONTENT, "UTF-8")))).thenReturn(new String[]{"NAT", "BKM"}); 

        OaiSetMatcherProcessor processor = createProcessor(jsWorker);
        processor.process(queueJob);
                
        RecordSet[] sets = rawrepoOaiDao.fetchSets(AGENCY_ID + ":" + BIB_REC_ID);
        assertEquals(2, sets.length);
        
        assertEquals("bkm", sets[0].setSpec);
        assertEquals(false, sets[0].gone);
        
        assertEquals("nat", sets[1].setSpec);
        assertEquals(false, sets[1].gone);
        
        when(jsWorker.getOaiSets(eq(AGENCY_ID), eq(new String(RECORD_CONTENT, "UTF-8")))).thenReturn(new String[]{"NAT"}); // Gone from BKM
        processor.process(queueJob);
        
        sets = rawrepoOaiDao.fetchSets(AGENCY_ID + ":" + BIB_REC_ID);
        assertEquals(2, sets.length);
        
        assertEquals("bkm", sets[0].setSpec);
        assertEquals(true, sets[0].gone);
        
        assertEquals("nat", sets[1].setSpec);
        assertEquals(false, sets[1].gone);
    }
    
    @Test
    public void testProcessQueueJob_whenRecordIsOriginal_itIsNotAdded() throws Exception {
        
        String bibrecid = "notexisting";
        JavaScriptWorker jsWorker = mock(JavaScriptWorker.class);
        OaiSetMatcherProcessor processor = createProcessor(jsWorker);
        processor.process(createQueueJob(bibrecid, AGENCY_ID));
        
        RecordSet[] sets = rawrepoOaiDao.fetchSets(AGENCY_ID + ":" + bibrecid);
        assertEquals(0, sets.length);        
    }
    
    private QueueJob createQueueJob(String bibrecid, int agencyId){
        QueueJob job = mock(QueueJob.class);
        RecordId recId = new RecordId(bibrecid, agencyId);
        when(job.getJob()).thenReturn(recId); 
        return job;
    }
    
}
