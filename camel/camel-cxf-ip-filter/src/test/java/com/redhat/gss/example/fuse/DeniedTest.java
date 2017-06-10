/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.gss.example.fuse;

import com.redhat.example.FabricCamelBlueprintTestSupport;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.apache.camel.model.ProcessorDefinition;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author dhawkins
 */
public class DeniedTest extends FabricCamelBlueprintTestSupport {

    private boolean debugBeforeMethodCalled;
    private boolean debugAfterMethodCalled;
    
    @Override
    public void setUp() throws Exception {
        System.setProperty("ips.allowed", "10.0.0.1|10.0.0.2");
        System.setProperty("ips.denied", "127.0.0.1|10.0.0.213");
        super.setUp();
    }

    @Override
    protected String getBlueprintDescriptor() {
        return "OSGI-INF/blueprint/blueprint.xml";
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
    
    @Override
    public boolean isUseAdviceWith() {
        return true;
    }

    @Test
    public void routeDenied() throws Exception {

        log.info("################################### STARTING ROUTE TEST ###################################");
        
        SOAPMessage expectedBody = createDefaultSoapMessage("foo");
                
        try {
            template.sendBody("http://127.0.0.1:8081/simplews/", ExchangePattern.InOut, expectedBody);
            Assert.fail("Expect exception here");
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.assertTrue("Caught exception: ", ex.getCause() instanceof HttpOperationFailedException);
            Assert.assertEquals(403, ((HttpOperationFailedException)ex.getCause()).getStatusCode());
        }
    }
    
    public static SOAPMessage createDefaultSoapMessage(String requestMessage) {
        try {
            SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
            SOAPBody body = soapMessage.getSOAPPart().getEnvelope().getBody();
 
            QName payloadName = new QName("com.redhat.gss.example.fuse.service", "sayHello", "ns1");
 
            SOAPBodyElement payload = body.addBodyElement(payloadName);
            
            SOAPElement message = payload.addChildElement("name");
             
            message.addTextNode(requestMessage);
            
            return soapMessage;
        } catch (SOAPException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
