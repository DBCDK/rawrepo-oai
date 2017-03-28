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
package dk.dbc.rawrepo.jms;

import dk.dbc.rawrepo.QueueJob;
import javax.jms.JMSException;
import javax.jms.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class JMSJobProcessor implements Runnable {
    
    protected static final Logger log = LoggerFactory.getLogger(JMSJobProcessor.class);
    
    private final JMSFetcher jmsFetcher;
    private int fetchMessageTimeoutMs = 10000;
    private int commitInterval = 100;
    private final SleepHandler sleepHandler;
    
    public JMSJobProcessor(JMSFetcher jmsFetcher){
        this.jmsFetcher = jmsFetcher;  
        this.sleepHandler = new SleepHandler()
                .withLowerLimit(10, 1000)
                .withLowerLimit(100, 60000);
    }
    public JMSJobProcessor withCommitInterval(int commitInterval){
        this.commitInterval = commitInterval;
        return this;
    }
    
    public JMSJobProcessor withFetchMessageTimeoutMs(int timeout){
        this.fetchMessageTimeoutMs = timeout;
        return this;
    }

    @Override
    public void run() {
        
        long processed = 0;
        
        while(true){
            QueueJob job = null;
            try {
                job = fetch();                
                if (job != null) {
                    process(job);
                    if (processed++ % commitInterval == 0) {      
                        commit();
                        jmsFetcher.commit();
                    }
                } else {
                    commit();
                    jmsFetcher.commit();
                }
                sleepHandler.reset();
            } catch (Exception ex) {
                log.error("Error processing job: {}, reason: {}", job, ex.getMessage());
                log.debug("Error processing job: {}", job, ex);
                try {
                    rollback();
                } catch (Exception e) {
                    log.error("Error rolling back", e);
                }
                try {
                    jmsFetcher.rollback();
                } catch (Exception e) {
                    log.error("Error rolling back", e);
                }
                sleepHandler.failure();
            }
        }        
    }
    
    private QueueJob fetch() throws JMSException {
        
        Message message = jmsFetcher.fetchMessage(fetchMessageTimeoutMs);
        if (message == null) {
            log.debug("Got no message");
            return null;
        }

        Object body = message.getBody(Object.class);
        String queueName = jmsFetcher.queueName(message);
 
        log.debug("got: " + body.toString() + " from: " + queueName);

        try {
            message.acknowledge();
        } catch (JMSException ex) {
            log.error("Could not ack: " + ex.getMessage());
        }

        return (QueueJob) body;
    }
    
    protected abstract void process(QueueJob job) throws Exception;
    protected abstract void rollback() throws Exception;
    protected abstract void commit() throws Exception;
    
}
