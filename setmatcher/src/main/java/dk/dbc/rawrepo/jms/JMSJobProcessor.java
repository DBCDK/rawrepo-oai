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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import dk.dbc.rawrepo.QueueJob;
import javax.jms.JMSException;
import javax.jms.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract JMS rawrepo job processor.
 * Handles the connection to the JMS broker, and let the extending classes handle 
 * the actual QueueJob processing.
 */
public abstract class JMSJobProcessor implements Runnable {
    
    protected static final Logger log = LoggerFactory.getLogger(JMSJobProcessor.class);
    
    private final JMSFetcher jmsFetcher;
    private int fetchMessageTimeoutMs = 10000;
    private int commitInterval = 100;
    private final SleepHandler sleepHandler;
    private final Timer processJobTimer;
    private final Timer rollbackTimer;
    private final Timer commitTimer;
    
    public JMSJobProcessor(JMSFetcher jmsFetcher, MetricRegistry metrics){
        this.jmsFetcher = jmsFetcher;  
        this.sleepHandler = new SleepHandler()
                .withLowerLimit(10, 1000)
                .withLowerLimit(100, 60000);
        this.processJobTimer = metrics.timer(getClass().getCanonicalName() + ".processJob");
        this.commitTimer = metrics.timer(getClass().getCanonicalName() + ".commit");
        this.rollbackTimer = metrics.timer(getClass().getCanonicalName() + ".rollback");
        
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
                    try(Timer.Context time = processJobTimer.time()){
                        process(job);
                    }
                }
                if(job == null || processed++ % commitInterval == 0){
                    try(Timer.Context time = commitTimer.time()){
                        commit();
                        jmsFetcher.commit();
                    }
                }
                sleepHandler.reset();
            } catch (Exception ex) {
                log.error("Error processing job: {}, reason: {}", job, ex.getMessage());
                log.debug("Error processing job: {}", job, ex);
                try(Timer.Context time = rollbackTimer.time()){
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
