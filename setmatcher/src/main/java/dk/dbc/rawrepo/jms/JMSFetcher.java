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

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.sun.messaging.ConnectionConfiguration;
import com.sun.messaging.ConnectionFactory;
import static dk.dbc.rawrepo.jms.JMSJobProcessor.log;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class JMSFetcher {
    
    protected static final Logger log = LoggerFactory.getLogger(JMSFetcher.class);

    private final HashMap<Destination, String> queueNames = new HashMap<>();
    private final ConnectionFactory connectionFactory;
    private JMSContext context;
    private List<JMSConsumer> queueConsumers;
    private final Counter taken;
    private final List<String> queues;

    public JMSFetcher(MetricRegistry metrics, String address, List<String> queues) throws JMSException {
        this.queues = queues;
        this.taken = metrics.counter(getClass().getCanonicalName() + ".taken");
        this.connectionFactory = new ConnectionFactory();
        connectionFactory.setProperty(ConnectionConfiguration.imqAddressList, address);
    }
    
    public void init() {
        log.info("Initializing JMS context");
        this.context = connectionFactory.createContext(Session.SESSION_TRANSACTED);

        this.queueConsumers = queues.stream()
                .map(queueName -> {
                    Queue queue = context.createQueue(queueName);
                    queueNames.put(queue, queueName);
                    return queue;
                })
                .map(queue -> context.createConsumer(queue))
                .collect(Collectors.toList());
        context.start();
    }

    public Message fetchMessage(long timeout) {
        Message message = queueConsumers.stream()
                .map(queue -> queue.receiveNoWait())
                .filter(msg -> msg != null)
                .findFirst()
                .orElseGet(() -> queueConsumers.get(0).receive(timeout));
        if (message != null) {
            taken.inc();
        }
        return message;
    }

    public void commit() {
        if (context.getTransacted()) {
            context.commit();
        }
    }

    public void rollback() {
        try {
            if (context.getTransacted()) {
                context.rollback();
            }
        } catch(RuntimeException e) {
            log.warn("Error rolling back", e);
        }
    }

    public String queueName(Message m) throws JMSException {
        return queueNames.getOrDefault(m.getJMSDestination(), "Unknown");
    }

    public void close() {
        try {
            context.stop();
            queueConsumers.stream().forEach(queue -> queue.close());
            context.close();
        } catch(Exception e) {
            log.warn("Unable to close jms context");
        }
    }

}
