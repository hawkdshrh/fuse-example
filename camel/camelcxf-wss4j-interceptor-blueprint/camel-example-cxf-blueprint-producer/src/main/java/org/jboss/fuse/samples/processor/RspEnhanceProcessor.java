package org.jboss.fuse.samples.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.Name;
import org.apache.camel.component.cxf.DataFormat;
import org.apache.camel.component.cxf.CxfConstants;

import com.sun.xml.messaging.saaj.soap.MessageImpl;
import com.sun.xml.messaging.saaj.soap.ver1_1.Message1_1Impl;

import org.apache.camel.example.reportincident.OutputReportIncident;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RspEnhanceProcessor implements Processor  {
    protected final transient Logger log = LoggerFactory.getLogger(RspEnhanceProcessor.class);
    
    public void process(Exchange exchange) throws Exception {
        DataFormat dataFormat = (DataFormat)exchange.getProperty(CxfConstants.DATA_FORMAT_PROPERTY);
        switch (dataFormat) {
        case PAYLOAD:
            log.info("PAYLOAD DataFormat is used!");
            exchange.getOut().setBody(responseForPayload());
            break;
        case POJO:
            log.info("POJO DataFormat is used!");
            exchange.getOut().setBody(responseForPayload());
            break;
        case CXF_MESSAGE:
            log.info("CXF_MESSAGE DataFormat is used!");
            MessageImpl body = (MessageImpl)exchange.getIn().getBody();
            exchange.getOut().setBody(new Message1_1Impl(responseForCxfMessage(body)));
            break;
        default:
            log.info(dataFormat + " is ignore!");
            break;
        }
    }
    
    private SOAPMessage responseForCxfMessage(MessageImpl message) {
        try {
            SOAPPart soapPart = message.getSOAPPart();
            SOAPEnvelope soapEnvelope = soapPart.getEnvelope(); 
            soapEnvelope.getHeader().detachNode();
           // soapEnvelope.normalize();
        } catch (SOAPException soapEx) {
            log.error("caught a SOAPException");
        }         
        return message;
    }

    private SOAPMessage responseForCxfMessage() {
        SOAPMessage message = null;
        try {
            MessageFactory myMsgFct = MessageFactory.newInstance();
            message = myMsgFct.createMessage();
            SOAPPart soapPart = message.getSOAPPart();
            SOAPEnvelope soapEnvelope = soapPart.getEnvelope(); 
            SOAPBody body = soapEnvelope.getBody(); 
            Name bodyName = soapEnvelope.createName("outputReportIncident", "ns2",
                                       "http://reportincident.example.camel.apache.org");

            SOAPBodyElement gltp = body.addBodyElement(bodyName); 
            Name myContent = soapEnvelope.createName("code");

            SOAPElement mySymbol = gltp.addChildElement(myContent); 
            mySymbol.addTextNode("OK");
        } catch (SOAPException soapEx) {
            log.error("caught a SOAPException");
        }         
        return message;
    }

    private OutputReportIncident responseForPayload() {
        OutputReportIncident ok = new OutputReportIncident();
        ok.setCode("OK");
        return ok;
    }
}
