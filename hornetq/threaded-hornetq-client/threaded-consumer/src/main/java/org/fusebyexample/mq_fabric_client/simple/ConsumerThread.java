/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fusebyexample.mq_fabric_client.simple;

import java.util.Timer;
import java.util.TimerTask;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TransactionRolledBackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dhawkins
 */
public class ConsumerThread extends Thread {

    private Session session = null;
    private Boolean transacted = false;
    private Connection connection = null;
    private final String selector;
    private final String user;
    private final String password;
    private MessageConsumer consumer = null;
    private final int threadNum;
    private final String clientPrefix;
    private final ConnectionFactory factory;
    private final Destination destination;
    private final int messageTimeoutMs;
    private final boolean transactionIsBatch;
    private final long transactionDelay;
    private final Timer timer = new Timer();
    private boolean isDone = false;

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerThread.class);

    public ConsumerThread(ConnectionFactory factory, Destination destination, int threadNum, String clientPrefix, int messageTimeoutMs, String selector, boolean transacted, boolean transactionIsBatch, long transactionDelay, String user, String password) {
        this.factory = factory;
        this.destination = destination;
        this.selector = selector;
        this.threadNum = threadNum;
        this.clientPrefix = clientPrefix;
        this.transacted = transacted;
        this.messageTimeoutMs = messageTimeoutMs;
        this.transactionIsBatch = transactionIsBatch;
        this.transactionDelay = transactionDelay;
        this.user = user;
        this.password = password;
    }

    @Override
    public void run() {

        try {
            if (transacted && transactionIsBatch) {
                timer.schedule(new CommitTask(), transactionDelay);
            }
            connection = factory.createConnection(user, password);
            connection.setClientID(clientPrefix + "." + destination + "-" + Integer.toString(this.threadNum) + "-" + Long.toString(System.currentTimeMillis()));
            connection.start();
            LOG.info("Started connection: " + connection.getClientID());
            if (transacted) {
                session = connection.createSession(transacted, Session.SESSION_TRANSACTED);
            } else {
                session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
            }
            LOG.info("Starting consumer: " + connection.getClientID());
            if (selector != null && !selector.isEmpty()) {
                LOG.info("With selector: " + selector);
                consumer = session.createConsumer(destination, selector);
            } else {
                consumer = session.createConsumer(destination);
            }
            
            boolean finished = false;
            int msgsRecd = 0;
            while (!finished) {

                Message message = consumer.receive(messageTimeoutMs);

                if (message != null) {
                    msgsRecd++;
                    LOG.info("Thread {}: {} : Got message {}; total: {}.", this.threadNum, System.currentTimeMillis(), message.getJMSMessageID(), msgsRecd);
                    if (transacted && !transactionIsBatch) {
                        Thread.sleep(transactionDelay);
                        try {
                            session.commit();
                            LOG.info("Thread {}: Acked message {}.", this.threadNum, message.getJMSMessageID());
                        } catch (TransactionRolledBackException ex) {
                            LOG.info("Thread {}: Rolling back message {}.", this.threadNum, message.getJMSMessageID());
                            session.rollback();
                        }
                    }

                } else {
                    finished = true;
                }
            }
            consumer.close();
            isDone = true;
        } catch (JMSException eJMS) {
            LOG.error("Caught JMSException: ", eJMS);
        } catch (InterruptedException eI) {
            LOG.error("Caught InterruptedException: ", eI);
            isDone = true;
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

    class CommitTask extends TimerTask {

        @Override
        public void run() {
            try {
                LOG.info("Committing session...");
                session.commit();
                timer.schedule(new CommitTask(), transactionDelay);
            } catch (JMSException ex) {
                LOG.error(null, ex);
            }
        }
    }

}
