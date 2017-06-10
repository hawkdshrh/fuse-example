package com.redhat.example.route;

import com.redhat.texample.processor.TimerLoggingEventNotifier;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

public class JmsQueueDrainRouteBuilder extends RouteBuilder {

    String jmsComponent = "source";

    boolean autoStartup = true;
    int maximumRedeliveries = 0;

    String jmsSourceDestinations;

    @Override
    public void configure() throws Exception {
        
        getContext().getManagementStrategy().addEventNotifier(new TimerLoggingEventNotifier());

        String[] sourceDestinations = jmsSourceDestinations.split(",");

        for (String sourceDestination : sourceDestinations) {

            fromF("%s:%s?", jmsComponent, sourceDestination)
                    .autoStartup(autoStartup)
                    .routeId("route-" + sourceDestination)
                    .log(LoggingLevel.INFO, "Archiving Message: $simple{header[JMSMessageID]}")
                    .recipientList().simple("file:work/jms/output/" + sourceDestination.replace(".", "-") + "/${header[JMSMessageID]}")
                    .toF("log://jms-queue-drain-throughput?level=DEBUG")
                    .toF("controlbus:route?routeId=" + "route-" + sourceDestination + "&action=stats");
        }
    }

    public String getJmsComponent() {
        return jmsComponent;
    }

    public void setJmsComponent(String jmsComponent) {
        this.jmsComponent = jmsComponent;
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
