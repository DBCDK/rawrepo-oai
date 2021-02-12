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
import com.codahale.metrics.Timer;
import dk.dbc.rawrepo.QueueJob;
import dk.dbc.rawrepo.RawRepoDAO;
import dk.dbc.rawrepo.Record;
import dk.dbc.rawrepo.RecordId;
import dk.dbc.rawrepo.oai.setmatcher.OaiSetMatcherDAO.RecordSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class OaiSetMatcherProcessor implements Runnable {
    
    protected static final Logger log = LoggerFactory.getLogger(OaiSetMatcherProcessor.class);
        
    private final DataSource rawrepo;
    private final DataSource rawrepoOai;
    private final JavaScriptWorker jsWorker;
    private final String worker;
    private final int commitInterval;
    private final int pollIntervalMs; 
    private final SleepHandler sleepHandler;
    private final Timer dequeueTimer;
    private final Timer processJobTimer;
    private final Timer commitTimer;
    
    
    public OaiSetMatcherProcessor(DataSource rawrepo, DataSource rawrepoOai, JavaScriptWorker jsWorker, 
            String worker, int commitInterval, int pollIntervalMs, MetricRegistry metrics) {
        
        this.rawrepo = rawrepo;        
        this.rawrepoOai = rawrepoOai;
        this.jsWorker = jsWorker;
        this.worker = worker;
        this.commitInterval = commitInterval;
        this.pollIntervalMs = pollIntervalMs;
        
        sleepHandler = new SleepHandler()
                            .withLowerLimit( 5, 1000 )      //adjacent failures > 5 -> sleep 1s
                            .withLowerLimit( 10, 10000 )    //adjacent failures > 10 -> sleep 10s
                            .withLowerLimit( 100, 60000 );  //adjacent failures > 100 -> sleep 60s
        
        dequeueTimer = metrics.timer("dequeueTimer");
        processJobTimer = metrics.timer("processJobTimer");
        commitTimer = metrics.timer("commitTimer");
        
        log.info("Initialized OaiSetMatcherProcessor");
    }
    
    /**
     * Processes a rawrepo queuejob in order to match a bibliographic item
     * with its corresponding OAI sets.
     * 
     * Based on the result of the JavaScript that does the actual set matching, 
     * this method will update the rawrepo-oai db.
     * 
     * @param job
     * @throws Exception 
     */
    static void process(QueueJob job, RawRepoDAO rawrepoDao, OaiSetMatcherDAO rawRepoOaiDao, 
            JavaScriptWorker jsWorker) throws Exception {
        
        log.info("Processing job {}", job);
        
        RecordId recordId = job.getJob();
        int agencyId = recordId.getAgencyId();
        String bibliographicRecordId = recordId.getBibliographicRecordId();
        
        if(agencyId == 870970 || agencyId == 870971) {
            
            // we need the unmerged rawrepo record
            // and the sets which it is currently in
            String pid = agencyId + ":" + bibliographicRecordId;
            Record record = rawrepoDao.fetchRecord(bibliographicRecordId, agencyId);
            RecordSet[] currentSets = rawRepoOaiDao.fetchSets(pid);

            // find the sets in which it is to be included
            HashSet<String> toBeIncludedIn;
            if (record.isOriginal() || record.isDeleted()) {
                toBeIncludedIn = new HashSet<>();
            } else {
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
    public void run() {                
                
        while(true) {
            
            boolean failed = false;
            int processed = 0;
            
            try(Connection rawrepoOaiConn = rawrepoOai.getConnection();
                    Connection rawrepoConn = rawrepo.getConnection()) {
                
                rawrepoConn.setAutoCommit(false);
                rawrepoOaiConn.setAutoCommit(false);
                
                try {
                    RawRepoDAO rawRepoDao = RawRepoDAO.builder(rawrepoConn).build();
                    OaiSetMatcherDAO oaiSetMatcherDAO = new OaiSetMatcherDAO(rawrepoOaiConn);
                    
                    Timer.Context time = dequeueTimer.time();
                    List<QueueJob> jobs = rawRepoDao.dequeue(worker, commitInterval);
                    time.close();
                    
                    if(jobs != null && !jobs.isEmpty()) {
                        
                        for (QueueJob job : jobs) {
                            time = processJobTimer.time();
                            process(job, rawRepoDao, oaiSetMatcherDAO, jsWorker);
                            time.stop();
                        }
                        
                        time = commitTimer.time();
                        rawrepoOaiConn.commit();
                        rawrepoConn.commit();
                        time.stop();   
                        
                        processed = jobs.size();                       
                    } 
                    
                } catch(Exception ex) {
                    log.debug("Error ocurred, rolling back", ex);
                    
                    try {
                        rawrepoOaiConn.rollback();                        
                    } catch(SQLException e) {
                        log.warn("Failed to roll back SetMatcher connection");
                    }
                    
                    try {
                        rawrepoConn.rollback();                        
                    } catch(SQLException e) {
                        log.warn("Failed to roll back RawRepo connection");
                    }
                    
                    throw ex;
                }
            
            } catch (Exception ex) {
                log.error("Failed to process jobs", ex);
                failed = true;                
            }

            
            if(failed) {
                sleepHandler.failure();
            } else if(processed > 0) {
                log.info("Processed and committed {} jobs", processed);
                sleepHandler.reset();                        
            } else {
                log.debug("Nothing to process, sleeping for {} ms", pollIntervalMs);
                try {                    
                    Thread.sleep(pollIntervalMs);
                } catch (InterruptedException ex) {
                    log.info("Interrupted while sleeping");
                }
            }

        }        
        
    }
    
}
