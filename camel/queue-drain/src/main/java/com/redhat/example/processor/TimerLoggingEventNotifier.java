package com.redhat.texample.processor;

import java.util.Date;
import java.util.EventObject;

import org.apache.camel.Exchange;
import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.apache.camel.management.event.ExchangeSentEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimerLoggingEventNotifier extends EventNotifierSupport {

    private final static Logger LOGGER = LoggerFactory.getLogger(TimerLoggingEventNotifier.class);

        @Override
        public void notify(EventObject event) throws Exception {
            LOGGER.info("SUMMARY: TEST" + event.getClass());
            if (event instanceof ExchangeSentEvent) {
                ExchangeSentEvent sent = (ExchangeSentEvent) event;
                LOGGER.info("LLNW SUMMARY: Took " + sent.getTimeTaken() + " millis to send to: " + sent.getEndpoint());
            }
            if (event instanceof ExchangeCompletedEvent) {;
            ExchangeCompletedEvent exchangeCompletedEvent = (ExchangeCompletedEvent) event;
            Exchange exchange = exchangeCompletedEvent.getExchange();
            String routeId = exchange.getFromRouteId();
            Date created = ((ExchangeCompletedEvent) event).getExchange().getProperty(Exchange.CREATED_TIMESTAMP, Date.class);
            // calculate elapsed time
            Date now = new Date();
            long elapsed = now.getTime() - created.getTime();
            LOGGER.info("LLNW SUMMARY Total: Took " + elapsed + " millis for the exchange on the route : " + routeId);
        }
      }
     
        @Override
        public boolean isEnabled(EventObject event) {
            // we only want the sent events
            LOGGER.info("LLNW SUMMARY: isEnabled " + event.getClass());
            return true;
        //    return event instanceof ExchangeSentEvent;
        }
     
        @Override
        protected void doStart() throws Exception {
          LOGGER.info("LLNW SUMMARY: SUMMARY: starting ");
        }
     
        @Override
        protected void doStop() throws Exception {
            LOGGER.info("LLNW SUMMARY: SUMMARY: stopping ");
        }
     
    }