package com.redhat.example.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DestEmbeddedActiveMQResource extends ExternalResource {
    Logger log = LoggerFactory.getLogger(this.getClass());

    boolean persistent = false;
    String brokerName = "localhost";

    BrokerService brokerService = new BrokerService();

    public DestEmbeddedActiveMQResource() {
    }

    public DestEmbeddedActiveMQResource(boolean persistent) {
        this.persistent = persistent;
    }

    public DestEmbeddedActiveMQResource(String brokerName) {
        this.brokerName = brokerName;
    }

    public DestEmbeddedActiveMQResource(String brokerName, boolean persistent) {
        this.persistent = persistent;
        this.brokerName = brokerName;
    }

    @Override
    protected void before() throws Throwable {
        log.info("Starting Embedded ActiveMQ Broker: Version {}", brokerService.BROKER_VERSION);

        brokerService.setBrokerName(brokerName);
        brokerService.setUseJmx(false);
        brokerService.setUseShutdownHook(false);
        if (persistent) {
            brokerService.setDataDirectory("target/activemq-data");
        } else {
            brokerService.setPersistent(false);
        }
        brokerService.addConnector("tcp://localhost:61617");
        brokerService.start();

        super.before();
        brokerService.waitUntilStarted();
    }

    @Override
    protected void after() {
        log.info("Stopping Embedded ActiveMQ Broker");

        super.after();

        try {
            brokerService.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ActiveMQConnectionFactory createConnectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerService.getVmConnectorURI().toString());
        connectionFactory.setBrokerURL(brokerService.getVmConnectorURI().toString());
        return connectionFactory;
    }

    public PooledConnectionFactory createPooledConnectionFactory() {
        ActiveMQConnectionFactory connectionFactory = createConnectionFactory();

        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory(connectionFactory);

        return pooledConnectionFactory;
    }

    /*****
     * Getters and Setters
     * @return 
     *****/
    
    public String getVmURL() {
        return brokerService.getVmConnectorURI().toString() + "?create=false";
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }


    /*****
     * Utility Methods
     * @param destinationName
     * @return 
     * @throws java.lang.Exception
     *****/
    public int getMessageCount(String destinationName) throws Exception {
        if (null == brokerService) {
            throw new IllegalStateException("BrokerService has not yet been created - was before() called?");
        }

        int messageCount = 0;
        for (Destination destination : brokerService.getBroker().getDestinationMap().values()) {
            if (destination.getName().equalsIgnoreCase(destinationName)) {
                messageCount = destination.getMessageStore().getMessageCount();
            }
        }

        return messageCount;
    }
}