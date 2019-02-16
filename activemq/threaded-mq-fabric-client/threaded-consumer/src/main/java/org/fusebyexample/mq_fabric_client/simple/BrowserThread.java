/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fusebyexample.mq_fabric_client.simple;

import java.util.Enumeration;
import java.util.Timer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQQueueBrowser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dhawkins
 */
public class BrowserThread extends Thread {

    private Session session = null;
    private Connection connection = null;
    private final String selector;
    private ActiveMQQueueBrowser browser = null;
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

    private boolean isDone = false;

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerThread.class);

    public BrowserThread(ConnectionFactory factory, Destination destination, int threadNum, String clientPrefix, int messageTimeoutMs, String selector, boolean transacted, boolean transactionIsBatch, long transactionDelay, long readDelay, boolean uniqueClientId) {
        this.factory = factory;
        this.destination = destination;
        this.selector = selector;
        this.threadNum = threadNum;
        this.clientPrefix = clientPrefix;
        this.messageTimeoutMs = messageTimeoutMs;
        this.transactionIsBatch = transactionIsBatch;
        this.transactionDelay = transactionDelay;
        this.readDelay = readDelay;
        this.uniqueClientId = uniqueClientId;
    }

    @Override
    public void run() {

        try {

            connection = factory.createConnection();
            if (uniqueClientId) {
                connection.setClientID(clientPrefix + "." + destination + "-" + Integer.toString(this.threadNum) + "-" + Long.toString(System.currentTimeMillis()));
            } else {
                connection.setClientID(clientPrefix + "." + destination + "-" + Integer.toString(this.threadNum));                
            }
            connection.start();
            LOG.info("Started consumer: " + connection.getClientID());
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            if (selector != null && !selector.isEmpty()) {
                LOG.info("With selector: " + selector);
                browser = (ActiveMQQueueBrowser)session.createBrowser((Queue) destination, selector);
            } else {
                browser = (ActiveMQQueueBrowser)session.createBrowser((Queue) destination);
            }

            boolean finished = false;
            while (!finished) {

                String browseResult = browser.getQueue().toString();
                LOG.info(browseResult);
                Enumeration enumeration = browser.getEnumeration();
                while (enumeration.hasMoreElements()) {
                    Object msg = enumeration.nextElement();
                    LOG.info(msg.getClass().getCanonicalName());
                    if (msg instanceof TextMessage) {
                        LOG.info(">>" + ((TextMessage)msg).getText());
                    }
                }

                if (readDelay > 0) {
                    Thread.sleep(readDelay);
                }
            }
            browser.close();
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
}
