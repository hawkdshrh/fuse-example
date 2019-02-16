package com.redhat.camel.cxfrs.example.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestCheckJSONInterceptor extends AbstractPhaseInterceptor<Message> {

    private final Logger LOG = LoggerFactory.getLogger(RestCheckJSONInterceptor.class);

    public RestCheckJSONInterceptor() {
        super(Phase.RECEIVE);
        this.addBefore(ValidationInterceptor.class.getName());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void handleMessage(Message message) throws Fault {
        LOG.info("Process user identification header info...");

        Map<String, List<String>> headers = CastUtils.cast((Map) message.get(Message.PROTOCOL_HEADERS));

        for (String headername : headers.keySet()) {

            StringBuilder builder = new StringBuilder();
            builder.append(headername).append(":").append(System.lineSeparator());
            for (String header : headers.get(headername)) {
                builder.append(header).append(System.lineSeparator());
            }
            LOG.info(builder.toString());

            if (headername.equalsIgnoreCase("content-type")) {
                List<String> contentTypes = headers.get(headername);
                if (contentTypes != null && !contentTypes.isEmpty()) {
                    for (String contentType : contentTypes) {
                        if (contentType != null && contentType.equalsIgnoreCase("application/json")) {
                            InterceptorChain chain = message.getInterceptorChain();
                            ValidationInterceptor validationInterceptor = new ValidationInterceptor();
                            validationInterceptor.addAfter(this.getClass().getName());
                            this.addBefore(ValidationInterceptor.class.getName());
                            validationInterceptor.setSchemaLocation("personSchema.json");
                            chain.add(validationInterceptor);
                        }
                    }
                }
            }
        }
    }
}
