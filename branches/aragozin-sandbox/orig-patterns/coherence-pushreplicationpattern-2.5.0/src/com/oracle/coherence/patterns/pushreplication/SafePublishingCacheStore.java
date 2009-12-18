/*
 * File: SafePublishingCacheStore.java
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

import com.oracle.coherence.patterns.pushreplication.EntryOperation.Operation;
import com.oracle.coherence.patterns.pushreplication.publishers.SafeLocalCachePublisher;
import com.tangosol.net.CacheFactory;

/**
 * <p>A {@link SafePublishingCacheStore} is an extension to the {@link PublishingCacheStore}
 * that additionally prevents potential re-entrancy of push replication when performing
 * multi-way replication between two or more clusters.</p>
 * 
 * @see SafeLocalCachePublisher
 *
 * @author Nicholas Gregory
 * @author Brian Oliver
 */
public class SafePublishingCacheStore extends PublishingCacheStore {
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param cacheName
	 */
	public SafePublishingCacheStore(String cacheName) {
		super(cacheName);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(Object key, Object value) {
		String siteName = CacheFactory.getCluster().getLocalMember().getSiteName();
		String clusterName = CacheFactory.getCluster().getClusterName();
		EntryOperation entryOperation = new EntryOperation(siteName, clusterName, getCacheName(), Operation.Store, key, value);
		
		if (SafeLocalCachePublisher.isSafeOperation(entryOperation)) {
			publishEntryOperation(entryOperation);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void storeAll(Map mapEntries) {
		String siteName           = CacheFactory.getCluster().getLocalMember().getSiteName();
		String clusterName        = CacheFactory.getCluster().getClusterName();
		EntryOperationBatch batch = new EntryOperationBatch(siteName, clusterName, getCacheName());
		
		for (Iterator iter = mapEntries.entrySet().iterator(); iter.hasNext(); ) {
			Map.Entry entry = (Map.Entry) iter.next();
			
			if (SafeLocalCachePublisher.isSafeOperation(getCacheName(), entry.getKey())) {
				batch.addEntryOperation(new EntryOperationBatch.EntryOp(Operation.Store, entry.getKey(), entry.getValue()));
			}
		}
		publishEntryOperationBatch(batch);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void erase(Object key) {
		String siteName = CacheFactory.getCluster().getLocalMember().getSiteName();
		String clusterName = CacheFactory.getCluster().getClusterName();
		EntryOperation entryOperation = new EntryOperation(siteName, clusterName, getCacheName(), Operation.Erase, key, null);
		
		if (SafeLocalCachePublisher.isSafeOperation(entryOperation)) {
			publishEntryOperation(entryOperation);
		}
	}
}
