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

import dk.dbc.rawrepo.jms.JMSJobProcessor;
import dk.dbc.rawrepo.QueueJob;
import dk.dbc.rawrepo.RawRepoDAO;
import dk.dbc.rawrepo.Record;
import dk.dbc.rawrepo.RecordId;
import dk.dbc.rawrepo.jms.JMSFetcher;
import dk.dbc.rawrepo.oai.setmatcher.OaiSetMatcherDAO.RecordSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javax.sql.DataSource;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class OaiSetMatcherProcessor extends JMSJobProcessor {
        
    final DataSource rawrepo;
    final DataSource rawrepoOai;
    final JavaScriptWorker jsWorker;
    Connection rawrepoConnection;
    Connection rawrepoOAIConnection;    
    
    public OaiSetMatcherProcessor(DataSource rawrepo, DataSource rawrepoOai, JavaScriptWorker jsWorker, JMSFetcher jmsFetcher) {
        super(jmsFetcher);
        this.rawrepo = rawrepo;        
        this.rawrepoOai = rawrepoOai;
        this.jsWorker = jsWorker;
        
        log.info("Initialized OaiSetMatcherProcessor");
    }
    
    @Override
    protected void process(QueueJob job) throws Exception {
        
        log.info("Processing job {}", job);
        
        RecordId recordId = job.getJob();
        int agencyId = recordId.getAgencyId();
        String bibliographicRecordId = recordId.getBibliographicRecordId();
        
        if(agencyId == 870970 || agencyId == 870971) {
            
            if(rawrepoOAIConnection == null) {
                setupConnections();
            }
                            
            // We need them DAO's
            RawRepoDAO rawrepoDao = RawRepoDAO.builder(rawrepoConnection).build();
            OaiSetMatcherDAO rawRepoOaiDao = new OaiSetMatcherDAO(rawrepoOAIConnection);

            // And we need the unmerged rawrepo record
            // and the sets which it is currently in
            String pid = agencyId + ":" + bibliographicRecordId;
            Record record = rawrepoDao.fetchRecord(bibliographicRecordId, agencyId);
            RecordSet[] currentSets = rawRepoOaiDao.fetchSets(pid);

            // find the sets in which it is to be included
            HashSet<String> toBeIncludedIn;
            if (record.isOriginal() || record.isDeleted()) {
                toBeIncludedIn = new HashSet<>();
            } else {
                rawRepoOaiDao.updateRecord(pid, false);
                String content = new String(record.getContent(), "UTF-8");
                toBeIncludedIn = new HashSet<>(Arrays.asList(jsWorker.getOaiSets(agencyId, content)));
            }

            // Update the timestamp of record if it is currently contained
            // or is going to be contained in any set
            // OAI-record is either created or updated
            if (currentSets.length > 0 || toBeIncludedIn.size() > 0) {
                rawRepoOaiDao.updateRecord(pid, record.isDeleted());
            }

            // Make sure record is gone from any set it is no longer included in
            List<String> removedFrom = new ArrayList<>();
            for (RecordSet recordSet : currentSets) {
                if (!toBeIncludedIn.contains(recordSet.setSpec.toUpperCase())) {
                    rawRepoOaiDao.updateSet(pid, recordSet.setSpec, true);
                    removedFrom.add(recordSet.setSpec.toUpperCase());
                }
            }

            // Lastly, include record in sets
            for (String set : toBeIncludedIn) {
                rawRepoOaiDao.updateSet(pid, set.toLowerCase(), false);
            }

            log.info("Harvest succes. Agency={}, bibRecId={}, sets={}, goneFrom={}",
                    agencyId, bibliographicRecordId, toBeIncludedIn, removedFrom);

        } else {
            log.debug("Agency not supported. Agency={}, bibRecId={}.", agencyId, bibliographicRecordId);
        }
    }            

    @Override
    protected void rollback() throws SQLException {
        if(rawrepoOAIConnection == null) {
            return;
        }
        // rollback oai conn, and close oai + rr
        try(Connection oai = rawrepoOAIConnection; 
                Connection rr = rawrepoConnection) {
            oai.rollback();
        } finally {
            rawrepoOAIConnection = null;
            rawrepoConnection = null;
        }
    }

    @Override
    protected void commit() throws SQLException {
        if(rawrepoOAIConnection == null) {
            return;
        }
        // commit oai conn, and close oai + rr
        try(Connection oai = rawrepoOAIConnection; 
                Connection rr = rawrepoConnection) {
            oai.commit();
            log.info("committed");
        } finally {
            rawrepoOAIConnection = null;
            rawrepoConnection = null;
        }
    }
    private void setupConnections() throws SQLException {
        rawrepoConnection = rawrepo.getConnection();
        rawrepoOAIConnection = rawrepoOai.getConnection();
        rawrepoOAIConnection.setAutoCommit(false);
    }
    
}
