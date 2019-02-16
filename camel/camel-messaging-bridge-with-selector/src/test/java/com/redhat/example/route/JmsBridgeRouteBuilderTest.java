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

public class JmsBridgeRouteBuilderTest extends CamelBlueprintTestSupport {

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
        return new String[]{"src/main/resources/repo/bridge.cfg", "bridge"};
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
    public EmbeddedActiveMQResource sourceBroker = new EmbeddedActiveMQResource("source", "tcp://nodex.redhat.com:61616", false);
    
    @Rule
    public EmbeddedActiveMQResource dest1Broker = new EmbeddedActiveMQResource("dest", "tcp://nodex.redhat.com:61617", false);
    
    @Rule
    public EmbeddedActiveMQResource dest2Broker = new EmbeddedActiveMQResource("dest", "tcp://nodex.redhat.com:61618", false);

    String endpoint1 = "source://topic:VirtualTopic.multi.test.redhat.receiveevent";

    @EndpointInject(uri = "mock://log://jms-bridge-throughput")
    MockEndpoint target;

    @Test(timeout = 60000)
    public void testRouteBuilder() throws Exception {

        context.getRouteDefinitions().get(0).adviceWith(context, new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // intercept sending to mock:foo and do something else
                interceptSendToEndpoint("log://jms-bridge-throughput?level=DEBUG")
                        .skipSendToOriginalEndpoint()
                        .to("mock://log://jms-bridge-throughput");
            }
        });
        
        context.getRouteDefinitions().get(1).adviceWith(context, new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // intercept sending to mock:foo and do something else
                interceptSendToEndpoint("log://jms-bridge-throughput?level=DEBUG")
                        .skipSendToOriginalEndpoint()
                        .to("mock://log://jms-bridge-throughput");
            }
        });

        log.info("********** Starting Test **********");

        int messageCount = 6;
        target.setExpectedMessageCount(messageCount);

        template.setDefaultEndpointUri(endpoint1);

        log.info("********** Sending 10001 **********");
        template.sendBodyAndHeader(TEST_MESSAGE_1, "MyProp", "10001");
        log.info("********** Sending 10002 **********");
        template.sendBodyAndHeader(TEST_MESSAGE_2, "MyProp", "10003");
        log.info("********** Sending 10003 **********");
        template.sendBodyAndHeader(TEST_MESSAGE_3, "MyProp", "10005");
        log.info("********** Sending 10004 **********");
        template.sendBodyAndHeader(TEST_MESSAGE_4, "MyProp", "10007");
        log.info("********** Sending 10005 **********");
        template.sendBodyAndHeader(TEST_MESSAGE_5, "MyProp", "10009");
        log.info("********** Sending 10006 **********");
        template.sendBodyAndHeader(TEST_MESSAGE_6, "MyProp", "10000");
        log.info("********** Sending 10007 **********");
        template.sendBodyAndHeader(TEST_MESSAGE_7, "MyProp", "10002");
        log.info("********** Sending 10008 **********");
        template.sendBodyAndHeader(TEST_MESSAGE_8, "MyProp", "10004");
        log.info("********** Sending 10009 **********");
        template.sendBodyAndHeader(TEST_MESSAGE_9, "MyProp", "10006");
        log.info("********** Sending 10000 **********");
        template.sendBodyAndHeader(TEST_MESSAGE_0, "MyProp", "10008");

        assertMockEndpointsSatisfied(30, TimeUnit.SECONDS);
        
    }

}
