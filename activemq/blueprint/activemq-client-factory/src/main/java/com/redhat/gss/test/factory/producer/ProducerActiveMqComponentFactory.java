package com.redhat.gss.test.factory.producer;

import com.redhat.gss.test.factory.CamelComponentFactory;
import com.redhat.gss.test.factory.consumer.pool.PoolAwareActiveMqComponent;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.camel.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerActiveMqComponentFactory implements CamelComponentFactory {

    Logger log = LoggerFactory.getLogger(this.getClass());

    String brokerURL;
    String userName;
    String userPassword;
    String cacheLevelName;

    public ProducerActiveMqComponentFactory() {
        log.info("Constructing ...");
    }

    @Override
    public Component createComponent() {
        log.info("CreatingComponent ...");

        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(userName, userPassword, brokerURL);
        PooledConnectionFactory pcf = new PooledConnectionFactory(cf);
        ActiveMQComponent cmp = new PoolAwareActiveMqComponent(pcf);
        cmp.setCacheLevelName(cacheLevelName);

        return cmp;
    }

    public String getBrokerUrl() {
        return brokerURL;
    }

    public void setBrokerUrl(String brokerURL) {
        this.brokerURL = brokerURL;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getCacheLevelName() {
        return cacheLevelName;
    }

    public void setCacheLevelName(String cacheLevelName) {
        this.cacheLevelName = cacheLevelName;
    }
}
