/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.activemq.gss.plugin;

import java.io.File;

import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.plugin.SubQueueSelectorCacheBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A plugin which allows the caching of the selector from a subscription queue.
 * <p/>
 * This stops the build-up of unwanted messages, especially when consumers may
 * disconnect from time to time when using virtual destinations.
 * <p/>
 * This is influenced by code snippets developed by Maciej Rakowicz
 *
 */
public class SubQueueNonNullSelectorCacheBroker extends SubQueueSelectorCacheBroker {

    private static final Logger LOG = LoggerFactory.getLogger(SubQueueNonNullSelectorCacheBroker.class);
    
    private boolean allowNullSelector;
    private boolean allowDefaultSelector;

    /**
     * Constructor
     */
    public SubQueueNonNullSelectorCacheBroker(Broker next, final File persistFile) {
        super(next, persistFile);
    }

    @Override
    public Subscription addConsumer(ConnectionContext context, ConsumerInfo info) throws Exception {
        // don't track selectors for advisory topics or temp destinations
        if (!AdvisorySupport.isAdvisoryTopic(info.getDestination()) && !info.getDestination().isTemporary()) {

            String destinationName = info.getDestination().getQualifiedName();
            LOG.debug("Caching consumer selector [{}] on  '{}'", info.getSelector(), destinationName);

            if (info.getSelector() == null && !this.allowNullSelector) {
                throw new IllegalArgumentException("Null Selectors are not allowed for Virtual consumers!");
            }
            
            if (info.getSelector().toUpperCase().equals("TRUE") && !this.allowDefaultSelector) {
                throw new IllegalArgumentException("Default Selectors are not allowed for Virtual consumers!");
            }
        }
        return super.addConsumer(context, info);
    }
    
    public void setAllowNullSelector(boolean allowNullSelector) {
        this.allowNullSelector = allowNullSelector;
    }
    
    public boolean getAllowNullSelector() {
        return this.allowNullSelector;
    }
    
    public void setAllowDefaultSelector(boolean allowDefaultSelector) {
        this.allowDefaultSelector = allowDefaultSelector;
    }
    
    public boolean getAllowDefaultSelector() {
        return this.allowDefaultSelector;
    }
    
}
