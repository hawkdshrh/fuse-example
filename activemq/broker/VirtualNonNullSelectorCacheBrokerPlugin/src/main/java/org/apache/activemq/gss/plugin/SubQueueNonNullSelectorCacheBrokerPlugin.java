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

import org.apache.activemq.broker.Broker;
import org.apache.activemq.plugin.SubQueueSelectorCacheBrokerPlugin;

/**
 * Enhancement to plugin which allows the caching of the selector from a subscription queue.
 * <p/>
 * This stops the build-up of unwanted messages, especially when consumers may
 * disconnect from time to time when using virtual destinations.
 * <p/>
 * This is influenced by code snippets developed by Maciej Rakowicz
 *
 * @org.apache.xbean.XBean element="virtualNonNullSelectorCacheBrokerPlugin"
 */
public class SubQueueNonNullSelectorCacheBrokerPlugin extends SubQueueSelectorCacheBrokerPlugin {

    private boolean allowNullSelector = false;
    private boolean allowDefaultSelector = false;

    @Override
    public Broker installPlugin(Broker broker) throws Exception {
        SubQueueNonNullSelectorCacheBroker rc = new SubQueueNonNullSelectorCacheBroker(broker, this.getPersistFile());
        rc.setSingleSelectorPerDestination(this.isSingleSelectorPerDestination());
        rc.setPersistInterval(this.getPersistInterval());
        rc.setIgnoreWildcardSelectors(this.isIgnoreWildcardSelectors());
        rc.setAllowNullSelector(allowNullSelector);
        rc.setAllowDefaultSelector(allowDefaultSelector);
        return rc;
    }

    public boolean isAllowNullSelector() {
        return allowNullSelector;
    }

    public void setAllowNullSelector(boolean allowNullSelector) {
        this.allowNullSelector = allowNullSelector;
    }    
    
    public boolean isAllowDefaultSelector() {
        return allowDefaultSelector;
    }    

    /**
     * @org.apache.xbean.Property alias=&quot;allowDefaultSelector&quot; nestedType=&quot;boolean&quot;
     */
    public void setAllowDefaultSelector(boolean allowDefaultSelector) {
        this.allowDefaultSelector = allowDefaultSelector;
    }

}
