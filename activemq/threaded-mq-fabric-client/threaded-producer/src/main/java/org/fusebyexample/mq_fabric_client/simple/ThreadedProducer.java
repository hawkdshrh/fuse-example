/*
 * Copyright (C) Red Hat, Inc.
 * http://www.redhat.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusebyexample.mq_fabric_client.simple;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.activemq.ActiveMQConnectionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadedProducer {

    private static final Logger LOG = LoggerFactory.getLogger(ThreadedProducer.class);

    private static int MESSAGE_DELAY_MILLISECONDS = 1;
    private static long MESSAGE_TIME_TO_LIVE_MILLISECONDS = 0;
    private static int NUM_MESSAGES_TO_BE_SENT_PER_DESTINATION = 1;
    private static int NUM_THREADS_PER_DESTINATION = 1;
    private static int MESSAGE_LENGTH = 0;
    private static boolean PRODUCER_USE_ASYNC = true;
    private static boolean TRANSACTED = false;
    private static boolean PERSISTENT = true;
    private static int PRODUCER_WINDOW_SIZE = 1024000;
    private static String CONNECTION_FACTORY_NAME = "myJmsFactory";
    private static String CLIENT_PREFIX = "client";
    private static String BODY = null;
    private static final String DESTINATIONS = "destinations";
    private static final String HEADERNAMES = "headers";
    private static final String PROPNAMES = "props";
    private static final Map<String,String> HEADERS = new HashMap<>();
    private static final Map<String,String> PROPS = new HashMap<>();

    public static void main(String args[]) throws IOException, InterruptedException {

        String brokerUrl = System.getProperty("java.naming.provider.url");

        if (brokerUrl != null) {
            LOG.info("******************************");
            LOG.info("Overriding jndi brokerUrl, now using: {}", brokerUrl);
            LOG.info("******************************");
        }

        try {

            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream("jndi.properties"));
            } catch (Exception ex) {
                properties.load(ThreadedProducer.class.getResourceAsStream("/jndi.properties"));
            }

            System.setProperty("java.naming.provider.url", properties.getProperty("java.naming.provider.url"));
            MESSAGE_DELAY_MILLISECONDS = Integer.parseInt(properties.getProperty("message.delay.ms"));
            MESSAGE_TIME_TO_LIVE_MILLISECONDS = Integer.parseInt(properties.getProperty("message.ttl.ms"));
            NUM_MESSAGES_TO_BE_SENT_PER_DESTINATION = Integer.parseInt(properties.getProperty("num.messages.per.dest"));
            NUM_THREADS_PER_DESTINATION = Integer.parseInt(properties.getProperty("num.threads.per.dest"));
            PRODUCER_USE_ASYNC = Boolean.parseBoolean(properties.getProperty("producer.use.async"));
            PRODUCER_WINDOW_SIZE = Integer.parseInt(properties.getProperty("producer.window.size"));
            MESSAGE_LENGTH = Integer.parseInt(properties.getProperty("message.length"));
            CONNECTION_FACTORY_NAME = properties.getProperty("connectionFactoryNames");
            CLIENT_PREFIX = properties.getProperty("client.prefix");
            TRANSACTED = Boolean.parseBoolean(properties.getProperty("transacted"));
            PERSISTENT = Boolean.parseBoolean(properties.getProperty("persistent"));
            BODY = properties.getProperty("body");

            String destinationNameList = properties.getProperty(DESTINATIONS);
            String[] destinationNames = destinationNameList.split(",");
            
            String headerNameList = properties.getProperty(HEADERNAMES);
            String[] headerNames = headerNameList.split(",");
            
            String propNameList = properties.getProperty(PROPNAMES);
            String[] propNames = propNameList.split(",");

            // JNDI lookup of JMS Connection Factory and JMS Destination
            Context context = new InitialContext();
            ActiveMQConnectionFactory factory = (ActiveMQConnectionFactory) context.lookup(CONNECTION_FACTORY_NAME);
            factory.setProducerWindowSize(PRODUCER_WINDOW_SIZE);
            factory.setAlwaysSyncSend(!PRODUCER_USE_ASYNC);
            factory.setUseAsyncSend(PRODUCER_USE_ASYNC);

            List<Destination> destinations = new ArrayList<>();

            for (String destinationName : destinationNames) {
                destinations.add((Destination) context.lookup(destinationName));
            }
            
            for (String headerName : headerNames) {
                HEADERS.put(headerName, properties.getProperty(headerName));
            }
            
            for (String propName : propNames) {
                PROPS.put(propName, properties.getProperty(propName));
            }

            List<ProducerThread> threads = new ArrayList<>();

            for (Destination destination : destinations) {

                for (int i = 0; i < NUM_THREADS_PER_DESTINATION; i++) {

                    ProducerThread producerThread = new ProducerThread(factory, destination, CLIENT_PREFIX, NUM_MESSAGES_TO_BE_SENT_PER_DESTINATION / NUM_THREADS_PER_DESTINATION, MESSAGE_DELAY_MILLISECONDS, MESSAGE_TIME_TO_LIVE_MILLISECONDS, i + 1, MESSAGE_LENGTH, TRANSACTED, PERSISTENT, BODY, PROPS, HEADERS);
                    producerThread.start();
                    threads.add(producerThread);
                }
            }
            for (Thread thread : threads) {
                thread.join();
            }

        } catch (NamingException eN) {
            LOG.error("Caught NamingException: ", eN);
        }
    }
}
