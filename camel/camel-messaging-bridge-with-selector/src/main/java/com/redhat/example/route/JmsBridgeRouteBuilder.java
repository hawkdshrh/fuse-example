package com.redhat.example.route;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

public class JmsBridgeRouteBuilder extends RouteBuilder {

    String jmsSourceComponent = "source";
    String jmsDest1Component = "dest1";
    String jmsDest2Component = "dest2";

    boolean autoStartup = true;
    int maximumRedeliveries = 0;

    String jmsSourceDestinations;
    String jmsSourceSelectors;

    @Override
    public void configure() throws Exception {
        
        String[] sourceDestinations = jmsSourceDestinations.split(",");
        String[] sourceSelectors = jmsSourceSelectors.split(";");

        for (int i = 0; i < sourceDestinations.length; i++) {
            
            String dest1 = jmsDest1Component + ":" + sourceDestinations[i];
            String dest2 = jmsDest2Component + ":" + sourceDestinations[i];

            fromF("%s:%s?selector=%s", jmsSourceComponent, sourceDestinations[i], sourceSelectors[i])
                    .autoStartup(autoStartup)
                    .routeId("route-" + sourceDestinations[i])
                    .toF("log://jms-bridge-throughput?level=DEBUG")
                    .loadBalance().roundRobin()
                    .to(dest1, dest2);
        }
    }

    public String getJmsSourceComponent() {
        return jmsSourceComponent;
    }

    public void setJmsSourceComponent(String jmsSourceComponent) {
        this.jmsSourceComponent = jmsSourceComponent;
    }

    public String getJmsSourceDestinations() {
        return jmsSourceDestinations;
    }

    public void setJmsSourceDestinations(String jmsSourceDestinations) {
        this.jmsSourceDestinations = jmsSourceDestinations;
    }
    
    public String getJmsSourceSelectors() {
        return jmsSourceSelectors;
    }

    public void setJmsSourceSelectors(String jmsSourceSelectors) {
        this.jmsSourceSelectors = jmsSourceSelectors;
    }
    
    public String getJmsDest1Component() {
        return jmsDest1Component;
    }

    public void setJmsDest1Component(String jmsDest1Component) {
        this.jmsDest1Component = jmsDest1Component;
    }
    
    public String getJmsDest2Component() {
        return jmsDest2Component;
    }

    public void setJmsDest2Component(String jmsDest2Component) {
        this.jmsDest2Component = jmsDest2Component;
    }

    public boolean isAutoStartup() {
        return autoStartup;
    }

    public void setAutoStartup(boolean autoStartup) {
        this.autoStartup = autoStartup;
    }

    public int getMaximumRedeliveries() {
        return maximumRedeliveries;
    }

    public void setMaximumRedeliveries(int maximumRedeliveries) {
        this.maximumRedeliveries = maximumRedeliveries;
    }

}
