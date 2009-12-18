/*
 * File: PushReplicationManager.java
 * 
 * Copyright (c) 2008. All Rights Reserved. Oracle Corporation.
 * 
 * Oracle is a registered trademark of Oracle Corporation and/or its
 * affiliates.
 * 
 * This software is the confidential and proprietary information of Oracle
 * Corporation. You shall not disclose such confidential and proprietary
 * information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Oracle Corporation.
 * 
 * Oracle Corporation makes no representations or warranties about 
 * the suitability of the software, either express or implied, 
 * including but not limited to the implied warranties of 
 * merchantability, fitness for a particular purpose, or 
 * non-infringement.  Oracle Corporation shall not be liable for 
 * any damages suffered by licensee as a result of using, modifying 
 * or distributing this software or its derivatives.
 * 
 * This notice may not be removed or altered.
 */
package com.oracle.coherence.patterns.pushreplication;

import com.oracle.coherence.patterns.messaging.SubscriptionIdentifier;
import com.tangosol.net.NamedCache;

/**
 * <p>A {@link PushReplicationManager} provides facilities to control
 * the registration of {@link BatchPublisher}s which are used to 
 * publish {@link EntryOperation}s occuring on {@link NamedCache}s 
 * (configured to use {@link PublishingCacheStore}s).</p>
 * 
 * @author Brian Oliver
 */
public interface PushReplicationManager {

	/**
	 * <p>Registers a {@link BatchPublisher} with the provided publisherName to 
	 * perform publishing of {@link EntryOperation}s that occur on the specified cache.</p>
	 * 
	 * <p>NOTE: Internally the publisherName is used as the name of a subscription
	 * on a topic that is named after the cache name.</p>
	 * 
	 * @param cacheName
	 * @param publisherName
	 * @param batchPublisher
	 */
	public SubscriptionIdentifier registerBatchPublisher(String cacheName, 
														 String publisherName, 
														 BatchPublisher batchPublisher);
}
