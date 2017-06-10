package com.redhat.gss.example.fuse.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IpAddressFilterProcessor implements Processor {

    private final Logger logger = LoggerFactory.getLogger(IpAddressFilterProcessor.class);
    private List<String> allowedList;
    private List<String> deniedList;

    public IpAddressFilterProcessor() {
        allowedList = new ArrayList<>();
        deniedList = new ArrayList<>();
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        org.apache.cxf.message.Message cxfMessage = exchange.getIn().getHeader(CxfConstants.CAMEL_CXF_MESSAGE, org.apache.cxf.message.Message.class);
        HttpServletRequest request = (HttpServletRequest) cxfMessage.get("HTTP.REQUEST");

        if (request != null) {
            String ipAddress = request.getRemoteAddr(); // get the client IP address
            logger.debug("remote ipAddress: {}", ipAddress);
            exchange.getOut().setHeader("SourceIP", ipAddress);

            // First deal with denied access to the address
            for (String deniedIpAddress : deniedList) {
                if (deniedIpAddress.equals(ipAddress)) {
                    exchange.getOut().setBody("IP address " + ipAddress + " is forbidden to access this resource.");
                    exchange.getOut().setHeader(org.apache.cxf.message.Message.RESPONSE_CODE, 403);
                    exchange.getOut().setFault(true);
                    exchange.getIn().setFault(true);
                    return;
                }
            }

            // Allow access to a collection of non-empty, continue processing, or that all the IP addresses are legitimate
            if (allowedList.size() > 0) {
                boolean contains = false;
                for (String allowedIpAddress : allowedList) {
                    if (allowedIpAddress.equals(ipAddress)) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    exchange.getOut().setBody("IP address " + ipAddress + " is not authorized to access this resource.");
                    exchange.getOut().setHeader(org.apache.cxf.message.Message.RESPONSE_CODE, 401);
                    exchange.getOut().setFault(true);
                    exchange.getIn().setFault(true);
                    return;
                }
            }
            exchange.getOut().setBody(exchange.getIn().getBody());
        }
    }

    public List<String> getAllowedList() {
        return allowedList;
    }

    public void setAllowedList(List<String> allowedList) {
        this.allowedList = allowedList;
    }

    public void setAllowedListString(String allowedListString) {
        if (allowedListString != null && allowedListString.trim().length() > 0) {
            allowedList.addAll(Arrays.asList(allowedListString.split("\\|")));
        }
    }

    public List<String> getDeniedList() {
        return deniedList;
    }

    public void setDeniedList(List<String> deniedList) {
        this.deniedList = deniedList;
    }

    public void setDeniedListString(String deniedListString) {
        if (deniedListString != null && deniedListString.trim().length() > 0) {
            deniedList.addAll(Arrays.asList(deniedListString.split("\\|")));
        }
    }

}
