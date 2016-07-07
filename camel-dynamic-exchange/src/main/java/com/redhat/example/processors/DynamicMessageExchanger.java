package com.redhat.example.processors;

import org.apache.camel.*;
import org.apache.camel.util.ExchangeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.util.concurrent.TimeoutException;

/**
 * There are many common patterns when you need to dynamically compute a message
 * URI and retrieve content, based on input parameters from the inbound exchange.
 *  
 * This Processor is a simple solution that will help to implement such scenario
 * using supported camel expressions.
 *
 * This processor is based on the DynamicPollEnricher code example provided by 
 * Andy Fedotov at https://gist.github.com/afedotov/c9565ac5a53a45662d30
 *
 */
public class DynamicMessageExchanger implements Processor {

    private static final Logger log = LoggerFactory.getLogger(DynamicMessageExchanger.class);

    private final long receiveTimeout = 5000;

    public DynamicMessageExchanger() {
    }

    @Override
    public void process(Exchange exchange) throws Exception {

        // Evaluate endpoint Expression in the Exchange context to populate dynamic values
        String populatedURI = exchange.getIn().getHeader("fileLocation", String.class);
        log.info("Trying to replace message with payload from URI = {}, receiveTimeout = {}", populatedURI, receiveTimeout);

        ConsumerTemplate consumerTemplate = exchange.getContext().createConsumerTemplate();
        try {

            // Receive Exchange from resource endpoint
            Exchange resourceExchange = doReceive(consumerTemplate, populatedURI);

            // If there are no message received from resource endpoint, we always assume it is erroneous behavior
            if (resourceExchange == null)
                throw new TimeoutException(MessageFormatter.format("There is not any message received from endpoint after {} seconds", receiveTimeout).getMessage());

            // Replace the original message with new exchange message.
            ExchangeHelper.prepareAggregation(exchange, resourceExchange);
            exchange.setIn(resourceExchange.getIn());
            
            // Resource Exchange synchronizations are handovered to original Exchange.
            // So if original Exchange is rolled back, enriching will be rolled back too
            exchange.setUnitOfWork(resourceExchange.getUnitOfWork());
            resourceExchange.handoverCompletions(exchange);
            consumerTemplate.doneUoW(exchange);
            
        } finally {
            // If we manually create ConsumeTemplate via CamelContext.createConsumerTemplate(),
            // then we should stop this Service in the end, to avoid threads/memory leakage.
            log.info("Stopping consumer...");
            consumerTemplate.stop();
        }

    }

    private Exchange doReceive(ConsumerTemplate consumerTemplate, String uri) {
        Exchange answer = null;
        if (receiveTimeout < 0) {
            log.debug("doReceive() starting blocking receive from endpoint URI = {}", uri);
            answer = consumerTemplate.receive(uri);
        } else if (receiveTimeout == 0) {
            log.debug("doReceive() starting non-blocking receive from endpoint URI = {}", uri);
            answer = consumerTemplate.receiveNoWait(uri);
        } else {
            log.debug("doReceive() starting timed blocking receive from endpoint URI = {}, receiveTimeout = {}", uri, receiveTimeout);
            answer = consumerTemplate.receive(uri, receiveTimeout);
            
        }
        return answer;
    }
}
