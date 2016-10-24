package com.redhat.gss.test.templates.scr.simple;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

public class ScrRouteBuilder extends RouteBuilder {

    static final String CAMEL_ROUTE_ID_FORMAT = "{0}_SCR";
    static final String CAMEL_FROM_URI_FORMAT = "{0}";  // Expand on this later - maybe make it JMS
    public static final String SOURCE_COMPONENT = "source-component";
    public static final String DEST_COMPONENT = "dest-component";

    String sourceDestination;
    String outDestination;
    String actionOnMaxNak;
    String deadLetterURI;
    String exUnHandledExtension;

    public ScrRouteBuilder() {
    }

    @Override
    public void configure() throws Exception {
        ModelCamelContext context = getContext();

        String contextId = context.getName();

        logger.info("Entering configure() for context {}", contextId);

        String routeId = MessageFormat.format(CAMEL_ROUTE_ID_FORMAT, contextId);
        String toURI = "";

        String tmpFromURI = MessageFormat.format(CAMEL_FROM_URI_FORMAT, sourceDestination);

        logger.info("Configuring route using ROUTE_ID={} FROM_URI={}  TO_URI={}", routeId, tmpFromURI, toURI);

        errorHandler(deadLetterChannel(String
                .format("%s:queue:%s.%s.%s", SOURCE_COMPONENT, sourceDestination, contextId,
                        exUnHandledExtension)).useOriginalMessage().allowRedeliveryWhileStopping(false)
                .logStackTrace(true)
                .logHandled(true));

        onException()
                .logStackTrace(true)
                .backOffMultiplier(2)
                .useExponentialBackOff()
                .maximumRedeliveryDelay(20000)
                .logContinued(true)
                .logExhausted(true)
                .logExhaustedMessageHistory(false)
                .logHandled(true)
                .logRetryAttempted(true)
                .retryAttemptedLogLevel(LoggingLevel.WARN);

        RouteDefinition route = fromF("master:%s:%s:queue:%s.%s?transacted=true", contextId, SOURCE_COMPONENT, sourceDestination, contextId).autoStartup(true).routeId(contextId + "_R0_Consumer");
        
        route.setHeader("ReportType").constant("OUTBOUND")
                .setHeader("ContextID").constant(contextId).id("add contextId to header")
                .setHeader("Out").constant("SCR_OUT")
                .multicast().parallelProcessing().to("direct:logger", "direct:out");

        from("direct:logger").routeId(contextId + "_R1_Aggregate")
                .log(LoggingLevel.INFO, "Hello from SCR Simple Example 1.0.0!");

        from("direct:out").routeId(contextId + "_R1_Out")
                .toF("%s:queue:%s", DEST_COMPONENT, outDestination).id("publish delivery out");

    }

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public String getDeadLetterURI() {
        return deadLetterURI;
    }

    public void setDeadLetterURI(String deadLetterURI) {
        this.deadLetterURI = deadLetterURI;
    }

    public String getActionOnMaxNak() {
        return actionOnMaxNak;
    }

    public void setActionOnMaxNak(String actionOnMaxNak) {
        this.actionOnMaxNak = actionOnMaxNak;
    }
}
