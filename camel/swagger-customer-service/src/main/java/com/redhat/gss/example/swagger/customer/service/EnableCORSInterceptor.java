package com.redhat.gss.example.swagger.customer.service;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.Headers;

public class EnableCORSInterceptor extends AbstractPhaseInterceptor<Message> {

    public EnableCORSInterceptor() {
        super(Phase.PRE_PROTOCOL);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        Map<String, List<String>> headers = Headers.getSetProtocolHeaders(message);
        try {
            headers.put("Access-Control-Allow-Origin", Arrays.asList("*"));
            headers.put("Access-Control-Allow-Headers", Arrays.asList("origin", "content-type", "accept"));
            headers.put("Access-Control-Max-Age", Arrays.asList("10"));
            headers.put("Access-Control-Allow-Methods", Arrays.asList("POST", "GET", "PUT", "DELETE"));
        } catch (Exception ce) {
            throw new Fault(ce);
        }
    }
}