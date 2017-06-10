/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fusebyexample.mq_fabric_client.simple;

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
    private final String user;
    private final String password;
    private MessageProducer producer = null;
    private final ConnectionFactory factory;
    private final Destination destination;
    private final String clientPrefix;
    private final int iterations;
    private final int delay;
    private final int threadNum;
    private final long ttl;
    private final int messageLength;
    private final boolean transacted;
    private boolean isDone = false;

    static final String TEST_MESSAGE = "Test message:";
    private static final Logger LOG = LoggerFactory.getLogger(ProducerThread.class);

    public ProducerThread(ConnectionFactory factory, Destination destination, String clientPrefix, int iterations, int delay, long ttl, int threadNum, int messageLength, boolean transacted, String user, String password) {
        this.factory = factory;
        this.destination = destination;
        this.clientPrefix = clientPrefix;
        this.iterations = iterations;
        this.delay = delay;
        this.ttl = ttl;
        this.threadNum = threadNum;
        this.messageLength = messageLength;
        this.transacted = transacted;
        this.user = user;
        this.password = password;
    }

    @Override
    public void run() {

        try {
            connection = factory.createConnection(user, password);
            connection.setClientID(clientPrefix + "." + destination + "-" + Integer.toString(threadNum));
            connection.start();
            if (transacted) {
                session = connection.createSession(transacted, Session.SESSION_TRANSACTED);
            } else {
                session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
            }
            producer = session.createProducer(destination);
            producer.setTimeToLive(ttl);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            StringBuilder builder = new StringBuilder();
            Random random = new Random();
            for (int j = 0; j < messageLength; j++) {
                builder.append((char) (random.nextInt(94) + 32));
            }
            String padding = builder.toString();
            //LOG.info("Starting Production at: {} for {}", System.currentTimeMillis(), padding);
            for (int i = 1; i <= iterations; i++) {

                try {
                    //TextMessage message = session.createTextMessage(TEST_MESSAGE);
                    TextMessage message = session.createTextMessage("Sending message " + i + " at " + System.currentTimeMillis() + ": " + padding + ": to " + producer.getDestination() + " from " + threadNum);
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

            //LOG.info("Finished Production at: {}", System.currentTimeMillis());
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
