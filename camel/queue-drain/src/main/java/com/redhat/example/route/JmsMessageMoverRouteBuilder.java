package com.redhat.example.route;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

public class JmsMessageMoverRouteBuilder extends RouteBuilder {

    String srcJmsComponent = "source";
    String destJmsComponent = "dest";

    boolean autoStartup = true;
    int maximumRedeliveries = 0;

    String jmsSourceDestinations;

    @Override
    public void configure() throws Exception {
        
        String[] sourceDestinations = jmsSourceDestinations.split(",");

        for (String sourceDestination : sourceDestinations) {

            fromF("%s:%s?", srcJmsComponent, sourceDestination)
                    .autoStartup(autoStartup)
                    .routeId("route-" + sourceDestination)
                    .log(LoggingLevel.INFO, "Archiving Message: $simple{header[JMSMessageID]}")
                    .recipientList().simple("file:work/jms/output/" + sourceDestination.replace(".", "-") + "/${header[JMSMessageID]}")
                    .toF("log://jms-message-mover-throughput?level=DEBUG")
                    .toF("%s:%s?", destJmsComponent, sourceDestination);
        }
    }

    public String getSrcJmsComponent() {
        return srcJmsComponent;
    }

    public void setSrcJmsComponent(String jmsComponent) {
        this.srcJmsComponent = jmsComponent;
    }
    
    public String getDestJmsComponent() {
        return destJmsComponent;
    }

    public void setDestJmsComponent(String jmsComponent) {
        this.destJmsComponent = jmsComponent;
    }

    public String getJmsSourceDestinations() {
        return jmsSourceDestinations;
    }

    public void setJmsSourceDestinations(String jmsSourceDestinations) {
        this.jmsSourceDestinations = jmsSourceDestinations;
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
