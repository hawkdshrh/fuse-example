/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.camel.cxfrs.example.processor;

import java.security.Principal;
import java.util.Set;
import javax.security.auth.Subject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.karaf.jaas.config.JaasRealm;
import org.apache.karaf.management.JaasAuthenticator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dhawkins
 */
public class AuthZProcessor implements Processor {

    protected BundleContext context;
    protected ServiceReference sRefJaasRealm;
    protected JaasRealm realm;

    private static final Logger log = LoggerFactory.getLogger(AuthZProcessor.class);

    public AuthZProcessor() throws InvalidSyntaxException {
        context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        sRefJaasRealm = context.getServiceReference("org.apache.karaf.jaas.config.JaasRealm");
        realm = (JaasRealm) context.getService(sRefJaasRealm);
    }

    public void validateSubject(String username, String password) throws Exception {

        JaasAuthenticator jassAuthenticator = new JaasAuthenticator();
        jassAuthenticator.setRealm(realm.getName());

        String[] creds = {username, password};

        log.info("Realm:" + realm.getName());
        log.info("Trying login with " + creds[0] + " and " + creds[1]);

        try {
            Subject subject = jassAuthenticator.authenticate(creds);
            if (subject != null) {
                log.info("Login: Success!!!");
                Set<Principal> principals = subject.getPrincipals();
                for (Principal principal : principals) {
                    log.info(principal.toString());
                } 
            }
        } catch (Exception ex) {
            log.error("Login: Failed!!!");
        }
    }

    @Override
    public void process(Exchange exchng) throws Exception {
        
        
        
    }

}

