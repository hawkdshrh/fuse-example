package com.redhat.gss.test.factory.consumer.pool;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PoolAwareActiveMqComponent extends ActiveMQComponent {
    Logger log = LoggerFactory.getLogger(this.getClass());
    PooledConnectionFactory pooledConnectionFactory;

    public PoolAwareActiveMqComponent(PooledConnectionFactory pooledConnectionFactory) {
        log.trace( "Constructing ...");
        this.pooledConnectionFactory = pooledConnectionFactory;
        super.setConnectionFactory(pooledConnectionFactory);
    }

    public void setConnectionFactory(PooledConnectionFactory pooledConnectionFactory) {
        log.trace( "setConnectionFactory ...");
        super.setConnectionFactory(pooledConnectionFactory);
        this.pooledConnectionFactory = pooledConnectionFactory;
    }

    @Override
    protected void doStart() throws Exception {
        log.trace( "doStart ...");
        if ( null != pooledConnectionFactory ) {
            log.info("Starting PooledConnectionFactory");
            pooledConnectionFactory.start();
        }
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        log.trace( "doStop ...");
        super.doStop();
        if ( null != pooledConnectionFactory ) {
            log.info("Stopping PooledConnectionFactory");
            pooledConnectionFactory.stop();
        }
    }

}
