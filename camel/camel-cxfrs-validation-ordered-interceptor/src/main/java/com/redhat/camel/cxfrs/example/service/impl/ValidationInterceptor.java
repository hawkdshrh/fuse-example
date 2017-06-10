/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.camel.cxfrs.example.service.impl;

import com.redhat.camel.cxfrs.example.util.JsonValidatorUtil;
import java.io.IOException;
import java.io.InputStream;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationInterceptor extends AbstractPhaseInterceptor<Message> {
    
    private String schemaLocation;
    private final Logger LOG = LoggerFactory.getLogger(ValidationInterceptor.class);

    public ValidationInterceptor() {
        super(Phase.RECEIVE);
        this.addAfter(RestCheckJSONInterceptor.class.getName());
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        LOG.info("ValidationInterceptor :");
        String requestString = null;
        // Get the input stream
        InputStream is = message.getContent(InputStream.class);
        if (is != null) {
            CachedOutputStream bos = new CachedOutputStream();
            try {
                IOUtils.copy(is, bos);
                bos.flush();
                is.close();
                message.setContent(InputStream.class, bos.getInputStream());
                requestString = IOUtils.toString(bos.getInputStream());
                LOG.info("Request JSON :" + requestString);
                boolean status = JsonValidatorUtil.validateSchema(bos.getInputStream(), schemaLocation);
            } catch (IOException e) {
                throw new Fault(e);
            } catch (Exception e) {
                throw new Fault(e);
            }
        }

    }
    
    public void setSchemaLocation(String schemaLocation) {
        this.schemaLocation = schemaLocation;
    }
    
    public String getSchemaLocation() {
        return this.schemaLocation;
    }
}
