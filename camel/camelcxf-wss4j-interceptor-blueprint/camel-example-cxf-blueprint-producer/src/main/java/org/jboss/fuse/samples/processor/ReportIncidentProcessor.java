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

import com.sun.xml.messaging.saaj.soap.ver1_1.Message1_1Impl;

import org.apache.camel.example.reportincident.OutputReportIncident;

public class ReportIncidentProcessor implements Processor  {
    
    public void process(Exchange exchange) throws Exception {
        DataFormat dataFormat = (DataFormat)exchange.getProperty(CxfConstants.DATA_FORMAT_PROPERTY);
        switch (dataFormat) {
        case PAYLOAD:
            System.out.println("PAYLOAD DataFormat is used!");
            exchange.getOut().setBody(responseForPayload());
            break;
        case POJO:
            System.out.println("POJO DataFormat is used!");
            exchange.getOut().setBody(responseForPayload());
            break;
        case CXF_MESSAGE:
            System.out.println("CXF_MESSAGE DataFormat is used!");
            exchange.getOut().setBody(new Message1_1Impl(responseForCxfMessage()));
            break;
        default:
           System.out.println(dataFormat + " is ignore!");
             break;
        }
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
            System.out.println("caught a SOAPException");
        }         
        return message;
    }

    private OutputReportIncident responseForPayload() {
        OutputReportIncident ok = new OutputReportIncident();
        ok.setCode("OK");
        return ok;
    }
}
