package com.redhat.gss.example.fuse.processor;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.message.MessageContentsList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GreetingsProcessor implements Processor {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GreetingsProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        final MessageContentsList mcl = (MessageContentsList) exchange.getIn().getBody();
        for (final Object bodyPart : mcl) {
            if (bodyPart instanceof String) {
                final String greetings = "Hello there, " + bodyPart + "@" + exchange.getIn().getHeader("SourceIP");
                exchange.getIn().setBody(greetings);
                LOGGER.info(greetings);
            }
        }

    }
}
