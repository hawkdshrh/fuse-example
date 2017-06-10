/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fusebyexample.mq_fabric_client.simple;

import java.util.Map;
import java.util.Random;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dhawkins
 */
public class ProducerThread extends Thread {

    private Session session = null;
    private Connection connection = null;
    private MessageProducer producer = null;
    private final ConnectionFactory factory;
    private final Destination destination;
    private final String clientPrefix;
    private final int iterations;
    private final int delay;
    private final int threadNum;
    private final int messageLength;
    private final long ttl;    
    private final boolean transacted;
    private final boolean persistent;
    private boolean isDone = false;
    private final String messageBody;
    private final Map<String, String> additionalHeaders;
    private final Map<String, String> additionalProperties;
    private static final Logger LOG = LoggerFactory.getLogger(ProducerThread.class);

    public ProducerThread(ConnectionFactory factory, Destination destination, String clientPrefix, int iterations, int delay, long ttl, int threadNum, int messageLength, boolean transacted, boolean persistent, String messageBody, Map additionalProperties, Map additionalHeaders) {
        this.factory = factory;
        this.destination = destination;
        this.clientPrefix = clientPrefix;
        this.iterations = iterations;
        this.delay = delay;
        this.ttl = ttl;
        this.threadNum = threadNum;
        this.messageLength = messageLength;
        this.transacted = transacted;
        this.persistent = persistent;
        this.messageBody = messageBody;
        this.additionalProperties = additionalProperties;
        this.additionalHeaders = additionalHeaders;

    }

    @Override
    public void run() {

        try {
            connection = factory.createConnection();
            connection.setClientID(clientPrefix + "." + destination + "-" + Integer.toString(threadNum));
            connection.start();
            if (transacted) {
                session = connection.createSession(transacted, Session.SESSION_TRANSACTED);
            } else {
                session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
            }
            producer = session.createProducer(destination);
            producer.setTimeToLive(ttl);
            if(persistent) {
                producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            } else {
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            }
            String padding = null;
            if (messageBody == null) {
                StringBuilder builder = new StringBuilder();
                Random random = new Random();
                for (int j = 0; j < messageLength; j++) {
                    builder.append((char) (random.nextInt(94) + 32));
                }
                padding = builder.toString();
            }

            LOG.info("Starting Production at: {} for {}", System.currentTimeMillis(), connection.getClientID());
            for (int i = 1; i <= iterations; i++) {

                try {
                    TextMessage message = null;
                    if (messageBody != null) {
                        message = session.createTextMessage(messageBody);
                    } else {
                        message = session.createTextMessage("Sending message " + i + " at " + System.currentTimeMillis() + ": " + padding + ": to " + producer.getDestination() + " from " + threadNum);
                    }
                    for (String key : additionalProperties.keySet()) {
                        message.setStringProperty(key, additionalProperties.get(key));
                    }
                    for (String key : additionalHeaders.keySet()) {
                        message.setStringProperty(key, additionalHeaders.get(key));
                    }
                    producer.send(message);
                    if (transacted) {
                        session.commit();
                    }
                    LOG.info("{} : Thread {}: Sent {}. message: {}", System.currentTimeMillis(), threadNum, i, message.getJMSMessageID());

                } catch (JMSException ex) {
                    LOG.error("Caught JMSException: ", ex);
                }
                if (delay > 0) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ex) {
                        isDone = true;
                    }
                }
            }
            producer.close();
            isDone = true;
        } catch (JMSException eJMS) {
            LOG.error("Caught JMSException: ", eJMS);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException ex) {
                    LOG.error("Caught JMSException: ", ex);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    LOG.error("Error closing connection", e);
                }
            }
        }
    }

    public boolean isDone() {
        return isDone;
    }

}
