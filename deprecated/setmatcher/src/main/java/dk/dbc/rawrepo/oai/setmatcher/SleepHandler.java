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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.LoggerFactory;

/**
 * May be used when handling task, and one wants to delay work
 * when a number of adjacent failures occur.
 * 
 * Call failure when a task fails and reset when a task has been handled with success.
 * 
 */
public class SleepHandler {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger( SleepHandler.class );
    
    final List<Delay> limits = new ArrayList<>();
    AtomicLong adjacentFailures = new AtomicLong(0);
    
    /** Finds the right limit in the lower-limit-list according 
     * to the number of adjacentFailures.
     * 
     * @return number of ms to sleep
     */
    int sleepFor() {
        
        Delay result = null;
        long failures = adjacentFailures.get();
        
        for ( Delay current : limits ) {
            if( failures > current.lowerLimit ) {
                if( result == null || result.lowerLimit < current.lowerLimit ) {
                    result = current;
                }
            }
        }
        
        if( result != null ) {
            return result.delay;
        }
        
        return 0;
        
    }
    
    public SleepHandler withLowerLimit( int lowerLimit, int delay ){
        log.info( "Setting lower limit of adjacent failures '{}' to delay '{}'", lowerLimit, delay );
        limits.add( new Delay( lowerLimit, delay ) );                             
        return this;
    }
    
    public void failure(){
        
        long failures = adjacentFailures.incrementAndGet();
        
        int sleepFor = sleepFor();
        
        if( sleepFor > 0 ) {
            try {
                log.info( "Adjacent failures: '{}'. Sleeping for {} ms", failures, sleepFor );
                Thread.sleep( sleepFor );
            } catch (InterruptedException ex) {
                log.info("Interrupted while sleeping");
            }
        }
    }
    
    public void reset(){
        adjacentFailures.set(0);
    }
    
    private static class Delay {
        final int lowerLimit;
        final int delay;
        
        public Delay( int lowerLimit, int delay ) {
            this.lowerLimit = lowerLimit;
            this.delay = delay;
        }
    }
}
