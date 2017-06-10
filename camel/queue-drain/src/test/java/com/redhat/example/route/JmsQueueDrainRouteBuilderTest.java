package com.redhat.example.route;

import com.redhat.example.activemq.EmbeddedActiveMQResource;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;

public class JmsQueueDrainRouteBuilderTest extends CamelBlueprintTestSupport {

    private boolean debugBeforeMethodCalled;
    private boolean debugAfterMethodCalled;

    static final String TEST_MESSAGE_1
            = "<test>TEST_MESSAGE_1</test>";
    static final String TEST_MESSAGE_2
            = "<test>TEST_MESSAGE_2</test>";
    static final String TEST_MESSAGE_3
            = "<test>TEST_MESSAGE_3</test>";
    static final String TEST_MESSAGE_4
            = "<test>TEST_MESSAGE_4</test>";
    static final String TEST_MESSAGE_5
            = "<test>TEST_MESSAGE_5</test>";
    static final String TEST_MESSAGE_6
            = "<test>TEST_MESSAGE_6</test>";
    static final String TEST_MESSAGE_7
            = "<test>TEST_MESSAGE_7</test>";
    static final String TEST_MESSAGE_8
            = "<test>TEST_MESSAGE_8</test>";
    static final String TEST_MESSAGE_9
            = "<test>TEST_MESSAGE_9</test>";
    static final String TEST_MESSAGE_0
            = "<test>TEST_MESSAGE_0</test>";

    @Override
    protected String getBlueprintDescriptor() {
        return "OSGI-INF/blueprint/blueprint.xml";
    }

    @Override
    protected String[] loadConfigAdminConfigurationFile() {
        return new String[]{"src/main/resources/repo/drain.test.cfg", "drain.test"};
    }

    @Override
    public boolean isUseDebugger() {
        // must enable debugger
        return true;
    }

    @Override
    protected void debugBefore(Exchange exchange, org.apache.camel.Processor processor, ProcessorDefinition<?> definition, String id, String label) {
        log.info("Before " + definition + " with body " + exchange.getIn().getBody());
        debugBeforeMethodCalled = true;
    }

    @Override
    protected void debugAfter(Exchange exchange, org.apache.camel.Processor processor, ProcessorDefinition<?> definition, String id, String label, long timeTaken) {
        log.info("After " + definition + " with body " + exchange.getIn().getBody());
        debugAfterMethodCalled = true;
    }

    @Rule
    public EmbeddedActiveMQResource sourceBroker = new EmbeddedActiveMQResource("localhost", false);

    String endpoint1 = "source://queue:q.Test1";
    String endpoint2 = "source://queue:q.Test2";

    @EndpointInject(uri = "mock://log://jms-queue-drain-throughput")
    MockEndpoint target;

    @Test(timeout = 60000)
    public void testRouteBuilder() throws Exception {

        context.getRouteDefinitions().get(0).adviceWith(context, new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // intercept sending to mock:foo and do something else
                interceptSendToEndpoint("log://jms-queue-drain-throughput?level=DEBUG")
                        .skipSendToOriginalEndpoint()
                        .to("mock://log://jms-queue-drain-throughput");
            }
        });
        
        context.getRouteDefinitions().get(1).adviceWith(context, new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // intercept sending to mock:foo and do something else
                interceptSendToEndpoint("log://jms-queue-drain-throughput?level=DEBUG")
                        .skipSendToOriginalEndpoint()
                        .to("mock://log://jms-queue-drain-throughput");
            }
        });

        log.info("********** Starting Test **********");

        int messageCount = 10;
        target.setExpectedMessageCount(messageCount);

        template.setDefaultEndpointUri(endpoint1);

        log.info("********** Sending 10001 **********");
        template.sendBodyAndHeader(TEST_MESSAGE_1, "JMSMessageID", "10001");
        log.info("********** Sending 10002 **********");
        template.sendBodyAndHeader(TEST_MESSAGE_2, "JMSMessageID", "10002");
        log.info("********** Sending 10003 **********");
        template.sendBodyAndHeader(TEST_MESSAGE_3, "JMSMessageID", "10003");
        log.info("********** Sending 10004 **********");
        template.sendBodyAndHeader(TEST_MESSAGE_4, "JMSMessageID", "10004");
        log.info("********** Sending 10005 **********");
        template.sendBodyAndHeader(TEST_MESSAGE_5, "JMSMessageID", "10005");

        template.setDefaultEndpointUri(endpoint2);

        log.info("********** Sending 10006 **********");
        template.sendBodyAndHeader(TEST_MESSAGE_6, "JMSMessageID", "10006");
        log.info("********** Sending 10007 **********");
        template.sendBodyAndHeader(TEST_MESSAGE_7, "JMSMessageID", "10007");
        log.info("********** Sending 10008 **********");
        template.sendBodyAndHeader(TEST_MESSAGE_8, "JMSMessageID", "10008");
        log.info("********** Sending 10009 **********");
        template.sendBodyAndHeader(TEST_MESSAGE_9, "JMSMessageID", "10009");
        log.info("********** Sending 10000 **********");
        template.sendBodyAndHeader(TEST_MESSAGE_0, "JMSMessageID", "10000");

        assertMockEndpointsSatisfied(30, TimeUnit.SECONDS);
        
    }

}
