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
package dk.dbc.rawrepo.oai.setmatcher.jms;

import dk.dbc.rawrepo.RawRepoDAO;
import dk.dbc.rawrepo.RawRepoException;
import dk.dbc.rawrepo.Record;
import dk.dbc.rawrepo.oai.setmatcher.db.OaiSetMatcherDAO;
import dk.dbc.rawrepo.oai.setmatcher.db.OaiSetMatcherDAO.RecordSet;
import dk.dbc.rawrepo.oai.setmatcher.javascript.JavaScriptWorker;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class RawRepoJobProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(RawRepoJobProcessor.class);
    
    private final DataSource rawrepo;
    private final DataSource rawrepoOai;
    private final JavaScriptWorker jsWorker;
    
    public RawRepoJobProcessor(DataSource rawrepo, DataSource rawrepoOai, JavaScriptWorker jsWorker){
        this.rawrepo = rawrepo;        
        this.rawrepoOai = rawrepoOai;
        this.jsWorker = jsWorker;
    }
    
    public void start() {
        while(true){
            System.out.println("Ima harvesting");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                
            }
        }
    }
    
    static void processQueueJob(String bibliographicRecordId, int agencyId, 
            Connection rawrepoConnection, Connection rawrepoOaiConnection,
            JavaScriptWorker jsWorker) throws SQLException, RawRepoException, UnsupportedEncodingException, Exception {
        
        if(agencyId == 870970 || agencyId == 870971) {

            // We need them DAO's
            RawRepoDAO rawrepoDao = RawRepoDAO.builder(rawrepoConnection).build();
            OaiSetMatcherDAO rawRepoOaiDao = new OaiSetMatcherDAO(rawrepoOaiConnection);

            // And we need the unmerged rawrepo record
            Record record = rawrepoDao.fetchRecord(bibliographicRecordId, agencyId);
            String pid = agencyId + ":" + bibliographicRecordId;

            // Update records timestamp and state, and find the sets in which it is to be included
            HashSet<String> toBeIncludedIn;
            if (record.isDeleted()) {
                rawRepoOaiDao.updateRecord(pid, true);
                toBeIncludedIn = new HashSet<>();
            } else {
                rawRepoOaiDao.updateRecord(pid, false);                
                String content = new String(record.getContent(), "UTF-8");
                toBeIncludedIn = new HashSet<>(Arrays.asList(jsWorker.getOaiSets(agencyId, content)));
            }

            // Make sure record is gone from any set it is no longer included in
            RecordSet[] currentSets = rawRepoOaiDao.fetchSets(pid);
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
}
