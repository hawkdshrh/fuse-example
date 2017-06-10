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
import java.util.List;
import java.util.Properties;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hornetq.jms.client.HornetQConnectionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadedConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(ThreadedConsumer.class);

    private static Boolean AUTO_GROUP = null;
    private static Boolean BLOCK_ON_ACKNOWLEDGE = null;
    private static Boolean CACHE_LARGE_MESSAGES = null;
    private static Boolean COMPRESS_LARGE_MESSAGES = null;
    private static Boolean FAILOVER_ON_INITIAL_CONNECTION = null;
    private static Boolean PREACKNOWLEDGE = null;
    private static Boolean USE_GLOBAL_POOLS = null;
    private static Boolean SESSION_TRANSACTED = false;
    private static Boolean TRANSACTION_IS_BATCH = false;

    private static Double RETRY_INTERVAL_MULTIPLIER = null;

    private static Integer CONFIRMATION_WINDOW_SIZE = null;
    private static Integer CONSUMER_MAX_RATE = null;
    private static Integer CONSUMER_WINDOW_SIZE = null;
    private static Integer DUPS_OK_BATCH_SIZE = null;
    private static Integer INITIAL_CONNECT_ATTEMPTS = null;
    private static Integer INITIAL_MESSAGE_PACKET_SIZE = null;
    private static Integer MIN_LARGE_MESSAGE_SIZE = null;
    private static Integer RECONNECT_ATTEMPTS = null;
    private static Integer SCHEDULED_THREAD_POOL_MAX_SIZE = null;
    private static Integer THREAD_POOL_MAX_SIZE = null;
    private static Integer TRANSACTION_BATCH_SIZE = null;
    private static Integer NUM_THREADS_PER_DESTINATION = 1;
    private static Integer MESSAGE_TIMEOUT_MILLISECONDS = 30000;

    private static Long CALL_FAILOVER_TIMEOUT = null;
    private static Long CALL_TIMEOUT = null;
    private static Long CLIENT_FAILURE_CHECK_PERIOD = null;
    private static Long CONNECTION_TTL = null;
    private static Long MAX_RETRY_INTERVAL = null;
    private static Long RETRY_INTERVAL = null;
    private static Long TRANSACTION_DELAY = 0L;

    private static String CLIENT_PREFIX = "client";
    private static String CONNECTION_LOADBALANCING_POLICY_CLASSNAME = null;
    private static String GROUP_ID = null;
    private static final String DESTINATIONS = "destinations";
    private static String CONNECTION_FACTORY_NAME = "myJmsFactory";
    private static String SELECTOR = null;
    private static String USER = null;
    private static String PASSWORD = null;

    public static void main(String args[]) throws InterruptedException, IOException {

        String brokerUrl = System.getProperty("java.naming.provider.url");

        if (brokerUrl != null) {
            LOG.info("******************************");
            LOG.info("Overriding jndi brokerUrl, now using: {}", brokerUrl);
            LOG.info("******************************");
        }
        //Connection connection = null;
        try {

            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream("jndi.properties"));
            } catch (Exception ex) {
                properties.load(ThreadedConsumer.class.getResourceAsStream("/jndi.properties"));
            }
            System.setProperty("java.naming.provider.url", properties.getProperty("java.naming.provider.url"));
            CONNECTION_FACTORY_NAME = properties.getProperty("connectionFactoryNames");

            if (properties.getProperty("auto.group") != null) {
                AUTO_GROUP = Boolean.parseBoolean(properties.getProperty("auto.group"));
            }
            if (properties.getProperty("block.on.acknowledge") != null) {
                BLOCK_ON_ACKNOWLEDGE = Boolean.parseBoolean(properties.getProperty("block.on.acknowledge"));
            }
            if (properties.getProperty("cache.large.messages") != null) {
                CACHE_LARGE_MESSAGES = Boolean.parseBoolean(properties.getProperty("cache.large.messages"));
            }
            if (properties.getProperty("compress.large.messages") != null) {
                COMPRESS_LARGE_MESSAGES = Boolean.parseBoolean(properties.getProperty("compress.large.messages"));
            }
            if (properties.getProperty("failover.on.initial.connection") != null) {
                FAILOVER_ON_INITIAL_CONNECTION = Boolean.parseBoolean(properties.getProperty("failover.on.initial.connection"));
            }
            if (properties.getProperty("preacknowledge") != null) {
                PREACKNOWLEDGE = Boolean.parseBoolean(properties.getProperty("preacknowledge"));
            }
            if (properties.getProperty("use.global.pools") != null) {
                USE_GLOBAL_POOLS = Boolean.parseBoolean(properties.getProperty("use.global.pools"));
            }
            if (properties.getProperty("session.transacted") != null) {
                SESSION_TRANSACTED = Boolean.parseBoolean(properties.getProperty("session.transacted"));
            }
            if (properties.getProperty("transacted.batch") != null) {
                TRANSACTION_IS_BATCH = Boolean.parseBoolean(properties.getProperty("transacted.batch"));
            }
            if (properties.getProperty("retry.interval.multiplier") != null) {
                RETRY_INTERVAL_MULTIPLIER = Double.parseDouble(properties.getProperty("retry.interval.multiplier"));
            }
            if (properties.getProperty("confirmation.window.size") != null) {
                CONFIRMATION_WINDOW_SIZE = Integer.parseInt(properties.getProperty("confirmation.window.size"));
            }
            if (properties.getProperty("consumer.max.rate") != null) {
                CONSUMER_MAX_RATE = Integer.parseInt(properties.getProperty("consumer.max.rate"));
            }
            if (properties.getProperty("consumer.window.size") != null) {
                CONSUMER_WINDOW_SIZE = Integer.parseInt(properties.getProperty("consumer.window.size"));
            }
            if (properties.getProperty("dups.ok.batch.size") != null) {
                DUPS_OK_BATCH_SIZE = Integer.parseInt(properties.getProperty("dups.ok.batch.size"));
            }
            if (properties.getProperty("initial.connect.attempts") != null) {
                INITIAL_CONNECT_ATTEMPTS = Integer.parseInt(properties.getProperty("initial.connect.attempts"));
            }
            if (properties.getProperty("initial.message.packet.size") != null) {
                INITIAL_MESSAGE_PACKET_SIZE = Integer.parseInt(properties.getProperty("initial.message.packet.size"));
            }
            if (properties.getProperty("min.large.message.size") != null) {
                MIN_LARGE_MESSAGE_SIZE = Integer.parseInt(properties.getProperty("min.large.message.size"));
            }
            if (properties.getProperty("reconnect.attempts") != null) {
                RECONNECT_ATTEMPTS = Integer.parseInt(properties.getProperty("reconnect.attempts"));
            }
            if (properties.getProperty("scheduled.thread.pool.max.size") != null) {
                SCHEDULED_THREAD_POOL_MAX_SIZE = Integer.parseInt(properties.getProperty("scheduled.thread.pool.max.size"));
            }
            if (properties.getProperty("thread.pool.max.size") != null) {
                THREAD_POOL_MAX_SIZE = Integer.parseInt(properties.getProperty("thread.pool.max.size"));
            }
            if (properties.getProperty("transaction.batch.size") != null) {
                TRANSACTION_BATCH_SIZE = Integer.parseInt(properties.getProperty("transaction.batch.size"));
            }
            if (properties.getProperty("num.threads.per.dest") != null) {
                NUM_THREADS_PER_DESTINATION = Integer.parseInt(properties.getProperty("num.threads.per.dest"));
            }
            if (properties.getProperty("message.timeout.ms") != null) {
                MESSAGE_TIMEOUT_MILLISECONDS = Integer.parseInt(properties.getProperty("message.timeout.ms"));
            }
            if (properties.getProperty("call.failover.timeout") != null) {
                CALL_FAILOVER_TIMEOUT = Long.parseLong(properties.getProperty("call.failover.timeout"));
            }
            if (properties.getProperty("call.timeout") != null) {
                CALL_TIMEOUT = Long.parseLong(properties.getProperty("call.timeout"));
            }
            if (properties.getProperty("client.failure.check.period") != null) {
                CLIENT_FAILURE_CHECK_PERIOD = Long.parseLong(properties.getProperty("client.failure.check.period"));
            }
            if (properties.getProperty("connection.ttl") != null) {
                CONNECTION_TTL = Long.parseLong(properties.getProperty("connection.ttl"));
            }
            if (properties.getProperty("max.retry.interval") != null) {
                MAX_RETRY_INTERVAL = Long.parseLong(properties.getProperty("max.retry.interval"));
            }
            if (properties.getProperty("retry.interval") != null) {
                RETRY_INTERVAL = Long.parseLong(properties.getProperty("retry.interval"));
            }
            if (properties.getProperty("transacted.delay") != null) {
                TRANSACTION_DELAY = Long.parseLong(properties.getProperty("transacted.delay"));
            }
            if (properties.getProperty("client.prefix") != null) {
                CLIENT_PREFIX = properties.getProperty("client.prefix");
            }
            if (properties.getProperty("client.prefix") != null) {
                CLIENT_PREFIX = properties.getProperty("client.prefix");
            }
            if (properties.getProperty("connection.loadbalancing.policy.classname") != null) {
                CONNECTION_LOADBALANCING_POLICY_CLASSNAME = properties.getProperty("connection.loadbalancing.policy.classname");
            }
            if (properties.getProperty("group.id") != null) {
                GROUP_ID = properties.getProperty("group.id");
            }
            if (properties.getProperty("selector") != null) {
                SELECTOR = properties.getProperty("selector");
            }
            if (properties.getProperty("java.naming.security.principal") != null) {
                USER = properties.getProperty("java.naming.security.principal");
            }
            if (properties.getProperty("java.naming.security.credentials") != null) {
                PASSWORD = properties.getProperty("java.naming.security.credentials");
            }

            String destinationNameList = properties.getProperty(DESTINATIONS);
            String[] destinationNames = destinationNameList.split(",");

            // JNDI lookup of JMS Connection Factory and JMS Destination
            Context context = new InitialContext(properties);
            ConnectionFactory factory = (ConnectionFactory) context.lookup(CONNECTION_FACTORY_NAME);

            if (AUTO_GROUP != null) {
                ((HornetQConnectionFactory) factory).setAutoGroup(AUTO_GROUP);
            }
            if (BLOCK_ON_ACKNOWLEDGE != null) {
                ((HornetQConnectionFactory) factory).setBlockOnAcknowledge(BLOCK_ON_ACKNOWLEDGE);
            }
            if (CACHE_LARGE_MESSAGES != null) {
                ((HornetQConnectionFactory) factory).setCacheLargeMessagesClient(CACHE_LARGE_MESSAGES);
            }
            if (CALL_FAILOVER_TIMEOUT != null) {
                ((HornetQConnectionFactory) factory).setCallFailoverTimeout(CALL_FAILOVER_TIMEOUT);
            }
            if (CALL_TIMEOUT != null) {
                ((HornetQConnectionFactory) factory).setCallTimeout(CALL_TIMEOUT);
            }
            if (CLIENT_FAILURE_CHECK_PERIOD != null) {
                ((HornetQConnectionFactory) factory).setClientFailureCheckPeriod(CLIENT_FAILURE_CHECK_PERIOD);
            }
            if (COMPRESS_LARGE_MESSAGES != null) {
                ((HornetQConnectionFactory) factory).setCompressLargeMessage(COMPRESS_LARGE_MESSAGES);
            }
            if (CONFIRMATION_WINDOW_SIZE != null) {
                ((HornetQConnectionFactory) factory).setConfirmationWindowSize(CONFIRMATION_WINDOW_SIZE);
            }
            if (CONNECTION_LOADBALANCING_POLICY_CLASSNAME != null) {
                ((HornetQConnectionFactory) factory).setConnectionLoadBalancingPolicyClassName(CONNECTION_LOADBALANCING_POLICY_CLASSNAME);
            }
            if (CONNECTION_TTL != null) {
                ((HornetQConnectionFactory) factory).setConnectionTTL(CONNECTION_TTL);
            }
            if (CONSUMER_MAX_RATE != null) {
                ((HornetQConnectionFactory) factory).setConsumerMaxRate(CONSUMER_MAX_RATE);
            }
            if (CONSUMER_WINDOW_SIZE != null) {
                ((HornetQConnectionFactory) factory).setConsumerWindowSize(CONSUMER_WINDOW_SIZE);
            }
            if (DUPS_OK_BATCH_SIZE != null) {
                ((HornetQConnectionFactory) factory).setDupsOKBatchSize(DUPS_OK_BATCH_SIZE);
            }
            if (FAILOVER_ON_INITIAL_CONNECTION != null) {
                ((HornetQConnectionFactory) factory).setFailoverOnInitialConnection(FAILOVER_ON_INITIAL_CONNECTION);
            }
            if (GROUP_ID != null) {
                ((HornetQConnectionFactory) factory).setGroupID(GROUP_ID);
            }
            if (INITIAL_CONNECT_ATTEMPTS != null) {
                ((HornetQConnectionFactory) factory).setInitialConnectAttempts(INITIAL_CONNECT_ATTEMPTS);
            }
            if (INITIAL_MESSAGE_PACKET_SIZE != null) {
                ((HornetQConnectionFactory) factory).setInitialMessagePacketSize(INITIAL_MESSAGE_PACKET_SIZE);
            }
            if (MAX_RETRY_INTERVAL != null) {
                ((HornetQConnectionFactory) factory).setMaxRetryInterval(MAX_RETRY_INTERVAL);
            }
            if (MIN_LARGE_MESSAGE_SIZE != null) {
                ((HornetQConnectionFactory) factory).setMinLargeMessageSize(MIN_LARGE_MESSAGE_SIZE);
            }
            if (PREACKNOWLEDGE != null) {
                ((HornetQConnectionFactory) factory).setPreAcknowledge(PREACKNOWLEDGE);
            }
            if (RECONNECT_ATTEMPTS != null) {
                ((HornetQConnectionFactory) factory).setReconnectAttempts(RECONNECT_ATTEMPTS);
            }
            if (RETRY_INTERVAL != null) {
                ((HornetQConnectionFactory) factory).setRetryInterval(RETRY_INTERVAL);
            }
            if (RETRY_INTERVAL_MULTIPLIER != null) {
                ((HornetQConnectionFactory) factory).setRetryIntervalMultiplier(RETRY_INTERVAL_MULTIPLIER);
            }
            if (SCHEDULED_THREAD_POOL_MAX_SIZE != null) {
                ((HornetQConnectionFactory) factory).setScheduledThreadPoolMaxSize(SCHEDULED_THREAD_POOL_MAX_SIZE);
            }
            if (THREAD_POOL_MAX_SIZE != null) {
                ((HornetQConnectionFactory) factory).setThreadPoolMaxSize(THREAD_POOL_MAX_SIZE);
            }
            if (TRANSACTION_BATCH_SIZE != null) {
                ((HornetQConnectionFactory) factory).setTransactionBatchSize(TRANSACTION_BATCH_SIZE);
            }
            if (USE_GLOBAL_POOLS != null) {
                ((HornetQConnectionFactory) factory).setUseGlobalPools(USE_GLOBAL_POOLS);
            }
            
            List<Destination> destinations = new ArrayList<>();

            for (String destinationName : destinationNames) {
                Destination destination = (Destination) context.lookup((String) properties.get(destinationName));
                destinations.add(destination);
            }

            List<ConsumerThread> threads = new ArrayList<>();

            for (Destination destination : destinations) {

                for (int i = 0; i < NUM_THREADS_PER_DESTINATION; i++) {
                    ConsumerThread consumerThread = new ConsumerThread(factory, destination, i + 1, CLIENT_PREFIX, MESSAGE_TIMEOUT_MILLISECONDS, SELECTOR, SESSION_TRANSACTED, TRANSACTION_IS_BATCH, TRANSACTION_DELAY, USER, PASSWORD);
                    consumerThread.start();
                    threads.add(consumerThread);
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
