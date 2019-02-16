/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fusebyexample.mq_fabric_client.simple;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TransactionRolledBackException;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.command.ActiveMQMessage;
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
    private MessageConsumer consumer = null;
    private final int threadNum;
    private final String clientPrefix;
    private final ConnectionFactory factory;
    private final Destination destination;
    private final int messageTimeoutMs;
    private final boolean transactionIsBatch;
    private final boolean uniqueClientId;
    private final long transactionDelay;
    private final long readDelay;
    private final Timer timer = new Timer();
    private final Executor executor = Executors.newCachedThreadPool();
    private Boolean isCommit = false;

    private boolean connected = false;
    private boolean isDone = false;

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerThread.class);

    public ConsumerThread(ConnectionFactory factory, Destination destination, int threadNum, String clientPrefix, int messageTimeoutMs, String selector, boolean transacted, boolean transactionIsBatch, long transactionDelay, long readDelay, boolean uniqueClientId) {
        this.factory = factory;
        this.destination = destination;
        this.selector = selector;
        this.threadNum = threadNum;
        this.clientPrefix = clientPrefix;
        this.transacted = transacted;
        this.messageTimeoutMs = messageTimeoutMs;
        this.transactionIsBatch = transactionIsBatch;
        this.transactionDelay = transactionDelay;
        this.readDelay = readDelay;
        this.uniqueClientId = uniqueClientId;
    }

    @Override
    public void run() {

        int msgsRecd = 0;

        try {

            while (!isDone) {

                while (!connected && !isDone) {

                    try {

                        connection = factory.createConnection();
                        if (uniqueClientId) {
                            connection.setClientID(clientPrefix + "." + destination + "-" + Integer.toString(this.threadNum) + "-" + Long.toString(System.currentTimeMillis()));
                        } else {
                            connection.setClientID(clientPrefix + "." + destination + "-" + Integer.toString(this.threadNum));
                        }
                        connection.start();
                        LOG.info("Started consumer: " + ((ActiveMQConnection)connection).getConnectionInfo().getConnectionId() + ":" + connection.getClientID());
                        if (transacted) {
                            session = connection.createSession(transacted, Session.SESSION_TRANSACTED);
                        } else {
                            session = connection.createSession(transacted, Session.CLIENT_ACKNOWLEDGE);
                        }

                        if (selector != null && !selector.isEmpty()) {
                            LOG.info("With selector: " + selector);
                            consumer = session.createConsumer(destination, selector);
                        } else {
                            consumer = session.createConsumer(destination);
                        }
                        if (transacted && transactionIsBatch) {
                            timer.scheduleAtFixedRate(new CommitTask(), transactionDelay, transactionDelay);
                        }
                        connected = true;
                    } catch (Exception ex) {
                        connected = false;
                        LOG.error("Unhandled exception while connecting.  Retrying...");
                        if (consumer != null) {
                            try {
                                LOG.info("Closing consumer: {}", this.threadNum);
                                consumer.close();
                            } catch (JMSException exC) {
                                LOG.error("Caught JMSException: ", exC);
                            }
                        }
                        if (session != null) {
                            try {
                                LOG.info("Closing session: {}", this.threadNum);
                                session.close();
                            } catch (JMSException exS) {
                                LOG.error("Caught JMSException: ", exS);
                            }
                        }
                        if (connection != null) {
                            try {
                                LOG.info("Closing connection: {}", this.threadNum);
                                connection.close();
                            } catch (JMSException e) {
                                LOG.error("Error closing connection", e);
                            }
                        }
                    }
                }

                try {

                    Message message = consumer.receive(messageTimeoutMs);

                    if (message != null) {
                        msgsRecd++;
                        LOG.info("Thread {}: {} : Got message {}; total: {} path: {}.", this.threadNum, System.currentTimeMillis(), message.getJMSMessageID(), msgsRecd, ((ActiveMQMessage) message).getBrokerPath());

                        if (transacted && !transactionIsBatch) {
                            Thread.sleep(transactionDelay);
                            try {
                                session.commit();
                                LOG.info("Thread {}: Acked message {}.", this.threadNum, message.getJMSMessageID());
                            } catch (TransactionRolledBackException ex) {
                                LOG.info("Thread {}: Rolling back message {}.", this.threadNum, message.getJMSMessageID());
                                session.rollback();
                            }
                        } else {

                            if (readDelay > 0) {
                                Thread.sleep(readDelay);
                            }
                            message.acknowledge();
                            LOG.info("Thread {}: Acked message {}.", this.threadNum, message.getJMSMessageID());
                        }
                        if (transacted && isCommit) {
                            try {
                                session.commit();
                                isCommit = false;
                                LOG.info("Thread {}: Acked message {}.", this.threadNum, message.getJMSMessageID());
                            } catch (TransactionRolledBackException ex) {
                                LOG.info("Thread {}: Rolling back message {}.", this.threadNum, message.getJMSMessageID());
                                session.rollback();
                            }
                        }
                    }
                } catch (JMSException eJMS) {
                    LOG.error("Caught JMSException: ", eJMS);
                    if (consumer != null) {
                        try {
                            LOG.info("Closing consumer: {}", this.threadNum);
                            consumer.close();
                        } catch (JMSException exC) {
                            LOG.error("Caught JMSException: ", exC);
                        }
                    }
                    if (session != null) {
                        try {
                            LOG.info("Closing session: {}", this.threadNum);
                            session.close();
                        } catch (JMSException exS) {
                            LOG.error("Caught JMSException: ", exS);
                        }
                    }
                    if (connection != null) {
                        try {
                            LOG.info("Closing connection: {}", this.threadNum);
                            connection.close();
                        } catch (JMSException e) {
                            LOG.error("Error closing connection", e);
                        }
                    }
                    connected = false;
                } catch (InterruptedException eI) {
                    LOG.error("Caught InterruptedException: ", eI);
                    isDone = true;
                }
            }
        } finally {
            if (consumer != null) {
                try {
                    LOG.info("Closing consumer: {}", this.threadNum);
                    consumer.close();
                } catch (JMSException ex) {
                    LOG.error("Caught JMSException: ", ex);
                }
            }
            if (session != null) {
                try {
                    LOG.info("Closing session: {}", this.threadNum);
                    session.close();
                } catch (JMSException ex) {
                    LOG.error("Caught JMSException: ", ex);
                }
            }
            if (connection != null) {
                try {
                    LOG.info("Closing connection: {}", this.threadNum);
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
            LOG.info("Dest: " + destination + " : Thread " + threadNum + ": Committing session...");
            isCommit = true;
        }
    }

}
