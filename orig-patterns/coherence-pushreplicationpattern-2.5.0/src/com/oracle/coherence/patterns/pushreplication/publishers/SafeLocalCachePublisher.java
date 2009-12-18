/*
 * File: SafeCachePublisher.java
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
package com.oracle.coherence.patterns.pushreplication.publishers;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.oracle.coherence.patterns.messaging.SubscriptionIdentifier;
import com.oracle.coherence.patterns.pushreplication.EntryOperation;
import com.oracle.coherence.patterns.pushreplication.SafePublishingCacheStore;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.InvocableMap.EntryProcessor;

/**
 * <p>A {@link SafeLocalCachePublisher} is an extension of a {@link LocalCachePublisher}
 * that ensures re-entrancy of publishing operations does not occur in other clusters
 * when performing multi-way push replication.  That is, it prevents publishing
 * operations to be sent back to the originating cluster.</p>
 *
 * @author Brian Oliver
 * @author Nicholas Gregory
 */
@SuppressWarnings("serial")
public class SafeLocalCachePublisher extends LocalCachePublisher {

	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public SafeLocalCachePublisher() {
		super();
	}
	
	
	/**
	 * <p>Standard Constructor (using a {@link CachePublisher.BruteForceConflictResolver},
	 * a default restartDelay of 1000ms and an indefinite number of consecutive failures).</p>
	 * 
	 * @param cacheName
	 */
	public SafeLocalCachePublisher(String cacheName) {
		super(cacheName);
	}
	
	
	/**
	 * <p>Standard Constructor (with a default restartDelay of 1000ms and
	 * an indefinite number of consecutive failures).</p>
	 * 
	 * @param cacheName
	 * @param conflictResolver
	 */
	public SafeLocalCachePublisher(String cacheName,
								   ConflictResolver conflictResolver) {
		super(cacheName, conflictResolver);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void start(SubscriptionIdentifier subscriptionIdentifier) {
		//we don't need to do anything when starting
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void stop(SubscriptionIdentifier subscriptionIdentifier) {
		//we don't need to do anything when stopping
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void publish(EntryOperation entryOperation) {
		try {
			super.publish(entryOperation);
		}  finally {
			completedOperation(entryOperation);
		}
		
	}
	
	
	/**
	 * <p>Signals that the specified {@link EntryOperation} is about to
	 * commence (in the cluster in which this method is executing)
	 * signifying that no other operation (of such kind that matches)
	 * should also commence (in the said cluster).</p>
	 * 
	 * @param entryOperation
	 */
	public static void commencingOperation(EntryOperation entryOperation) {
		NamedCache pendingOperationsCache = CacheFactory.getCache("pending-operations-" + entryOperation.getCacheName());
		pendingOperationsCache.put(entryOperation.getKey(), null);
	}


	/**
	 * <p>Determines if it is safe to perform the specified {@link EntryOperation}
	 * (in the cluster in which this method is executing).</p>
	 * 
	 * @param entryOperation
	 */
	public static boolean isSafeOperation(EntryOperation entryOperation) {
		NamedCache pendingOperationsCache = CacheFactory.getCache("pending-operations-" + entryOperation.getCacheName());
		return !pendingOperationsCache.containsKey(entryOperation.getKey());
		
		//TODO NSA remove this in preference to the method below
	}
	
	
	/**
	 * <p>Determines if it is safe to perform the specified {@link EntryOperation}
	 * (in the cluster in which this method is executing).</p>
	 * 
	 * @param cacheName  the name of the cache the entry resides in
	 * @param key        the key in the cache to perform an operation on
	 */
	public static boolean isSafeOperation(String cacheName, Object key) {
		NamedCache pendingOperationsCache = CacheFactory.getCache("pending-operations-" + cacheName);
		return !pendingOperationsCache.containsKey(key);
	}
	
	
	/**
	 * <p>Signals that the specified {@link EntryOperation} has been 
	 * completed (in the cluster where this is being executed) and that 
	 * similar {@link EntryOperation}s may now commence (in the said cluster).</p>
	 * 
	 * @param entryOperation
	 */
	public static void completedOperation(EntryOperation entryOperation) {
		NamedCache pendingOperationsCache = CacheFactory.getCache("pending-operations-" + entryOperation.getCacheName());
		pendingOperationsCache.remove(entryOperation.getKey());
	}
	
    /**
     * <p>Factory method for constructing EntryOperationProcessor objects.</p>
     * 
     * @param entryOperation    the EntryOperation to process
     * @param conflictResolver  the ConflictResolver to use
     */
    public EntryOperationProcessor instantiateEntryOperationProcessor(EntryOperation entryOperation, ConflictResolver conflictResolver) {
        return new EntryOperationProcessor(entryOperation, conflictResolver);
    }
	

	/**
	 * <p>An {@link EntryProcessor} that <strong>safely</strong> performs the provided 
	 * {@link EntryOperation} using the specified {@link CachePublisher.ConflictResolver}.</p>
	 * 
	 * <p>Safely means that the {@link EntryOperationProcessor} marks that we're in the 
	 * process of updating an {@link Entry} prior to performing the operation.  This
	 * allows us to check in our {@link SafePublishingCacheStore} if we need to 
	 * publish the said update (to another cluster).</p>
	 */
	public static class EntryOperationProcessor extends LocalCachePublisher.EntryOperationProcessor implements ExternalizableLite, PortableObject {
		
    	/**
         * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
         */
	    public EntryOperationProcessor()
		    {
		    }
	
	    /**
		 * <p>Standard Constructor.</p>
		 * 
		 * @param entryOperation
		 * @param conflictResolver
		 */
		public EntryOperationProcessor(EntryOperation entryOperation, ConflictResolver conflictResolver) {
		    super(entryOperation, conflictResolver);
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public Object process(Entry entry) {
			commencingOperation(entryOperation);
			return super.process(entry);
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void readExternal(DataInput in) throws IOException {
			super.readExternal(in);			
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void writeExternal(DataOutput out) throws IOException {
		    super.writeExternal(out);
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void readExternal(PofReader reader) throws IOException {
			super.readExternal(reader);
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void writeExternal(PofWriter writer) throws IOException {
			super.writeExternal(writer);
		}
	}
}
