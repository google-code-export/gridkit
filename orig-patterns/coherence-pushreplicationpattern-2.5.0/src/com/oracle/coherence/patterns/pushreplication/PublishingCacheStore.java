/*
 * File: PublishingCacheStore.java
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

import java.util.Iterator;
import java.util.Map;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.command.DefaultContextConfiguration;
import com.oracle.coherence.patterns.command.ContextConfiguration.ManagementStrategy;
import com.oracle.coherence.patterns.messaging.DefaultMessagingSession;
import com.oracle.coherence.patterns.messaging.Topic;
import com.oracle.coherence.patterns.pushreplication.EntryOperation.Operation;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.AbstractCacheStore;
import com.tangosol.net.cache.CacheStore;

/**
 * <p>A {@link PublishingCacheStore} will publish all cache operations
 * against the {@link CacheStore} to a {@link Topic} for the said
 * {@link NamedCache}.</p>
 * 
 * <p>This implementation depends on the Coherence Messaging Pattern</p>
 * 
 * <p>Copyright (c) 2008. All Rights Reserved. Oracle Corporation.</p>
 *
 * @see Publisher
 * @see PublishingSubscription
 * @see DefaultPublishingSubscription
 *
 * @author Brian Oliver
 */
public class PublishingCacheStore extends AbstractCacheStore {

	/**
	 * <p>The name of the cache for which we'll be publishing
	 * cache {@link Operation}s.</p>
	 */
	private String cacheName;
	
	
	/**
	 * <p>The {@link Topic} to which we'll be publishing cache
	 * {@link Operation}s.</p>
	 */
	private Identifier topicIdentifier;
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param cacheName
	 */
	public PublishingCacheStore(String cacheName) {
		this.cacheName = cacheName;	
		this.topicIdentifier = DefaultMessagingSession.getInstance().createTopic(cacheName, 
																				 new DefaultContextConfiguration(ManagementStrategy.DISTRIBUTED));
	}


	/**
	 * <p>Returns the name of the cache from which we're publishing operations.</p>
	 */
	public String getCacheName() {
		return cacheName;
	}
	

	/**
	 * {@inheritDoc}
	 */
	public Object load(Object key) {
		//this cache store does not provide loading capabilities
		return null;
	}
	
	
	/**
	 * <p>Publishes an {@link EntryOperation} to the {@link Topic} identified by the
	 * cache store topicIdentifier.</p>
	 * 
	 * @param entryOperation  the {@link EntryOperation} to publish
	 */
	protected void publishEntryOperation(EntryOperation entryOperation) {
		DefaultMessagingSession.getInstance().publishMessage(topicIdentifier, entryOperation);
	}
	
	
	/**
	 * <p>Publishes an {@link EntryOperationBatch} to the {@link Topic} identified by the
	 * cache store topicIdentifier.</p>
	 * 
	 * @param entryOperationBatch  the {@link EntryOperationBatch} to publish
	 */
	protected void publishEntryOperationBatch(EntryOperationBatch entryOperationBatch) {
		DefaultMessagingSession.getInstance().publishMessage(topicIdentifier, entryOperationBatch);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(Object key, Object value) {
		String siteName = CacheFactory.getCluster().getLocalMember().getSiteName();
		String clusterName = CacheFactory.getCluster().getClusterName();
		publishEntryOperation(new EntryOperation(siteName, clusterName, cacheName, Operation.Store, key, value));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void storeAll(Map mapEntries) {
		String siteName           = CacheFactory.getCluster().getLocalMember().getSiteName();
		String clusterName        = CacheFactory.getCluster().getClusterName();
		EntryOperationBatch batch = new EntryOperationBatch(siteName, clusterName, cacheName);
		
		for (Iterator iter = mapEntries.entrySet().iterator(); iter.hasNext(); ) {
			Map.Entry entry = (Map.Entry) iter.next();
			batch.addEntryOperation(new EntryOperationBatch.EntryOp(Operation.Store, entry.getKey(), entry.getValue()));
		}
		publishEntryOperationBatch(batch);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void erase(Object key) {
		String siteName = CacheFactory.getCluster().getLocalMember().getSiteName();
		String clusterName = CacheFactory.getCluster().getClusterName();
		publishEntryOperation(new EntryOperation(siteName, clusterName, cacheName, Operation.Erase, key, null));
	}
}
