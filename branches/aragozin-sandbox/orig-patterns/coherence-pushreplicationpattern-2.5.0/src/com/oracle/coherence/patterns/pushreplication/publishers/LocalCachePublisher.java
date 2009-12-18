/*
 * File: LocalCachePublisher.java
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
import com.oracle.coherence.patterns.pushreplication.Publisher;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.InvocableMap.EntryProcessor;
import com.tangosol.util.processor.AbstractProcessor;

/**
 * <p>A {@link LocalCachePublisher} is a {@link CachePublisher} implementation that will 
 * publish {@link EntryOperation}s to a <strong>Local</strong> {@link NamedCache} (ie: contained
 * in the Coherence Cluster in which the {@link CachePublisher} is executing).</p>
 * 
 * <p>This {@link Publisher} is very useful for publishing the values
 * from one cache to another in the same cluster.</p>
 *
 * <p>To publish {@link EntryOperation}s from to another cluster, you should use
 * a {@link RemoteInvocationPublisher} with a {@link LocalCachePublisher} OR
 * a {@link RemoteCachePublisher}.</p>
 *
 * @see CachePublisher
 * @see RemoteInvocationPublisher
 * @see RemoteCachePublisher
 *
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class LocalCachePublisher extends AbstractPublisher implements CachePublisher {

	/**
	 * <p>The name of the cache on which we'll perform {@link EntryOperation}s.</p>
	 */
	private String cacheName;

	
	/**
	 * <p>The {@link CachePublisher.ConflictResolver} that will be used to resolve and perform {@link EntryOperation}s.</p>
	 */
	private ConflictResolver conflictResolver;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public LocalCachePublisher() {
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param cacheName
	 * @param conflictResolver
	 * @param restartDelay
	 * @param totalConsecutiveFailuresBeforeSuspending
	 */
	public LocalCachePublisher(String cacheName,
							   ConflictResolver conflictResolver,
						  	   long restartDelay,
						  	   int totalConsecutiveFailuresBeforeSuspending) {
		super(restartDelay, totalConsecutiveFailuresBeforeSuspending);
		this.cacheName = cacheName;
		this.conflictResolver = conflictResolver;
	}
	
	
	/**
	 * <p>Standard Constructor (with a default restartDelay of 1000ms
	 * and an indefinite number of consecutive failures).</p>
	 * 
	 * @param cacheName
	 * @param conflictResolver
	 */
	public LocalCachePublisher(String cacheName,
							   ConflictResolver conflictResolver) {
		this(cacheName, conflictResolver, 1000, -1);
	}
	
	
	/**
	 * <p>Standard Constructor (using a {@link CachePublisher.BruteForceConflictResolver}
	 * and having an indefinite number of consecutive failures).</p>
	 * 
	 * @param cacheName
	 * @param restartDelay
	 */
	public LocalCachePublisher(String cacheName,
						  	   long restartDelay) {
		this(cacheName, new CachePublisher.BruteForceConflictResolver(), restartDelay, -1);
	}

	
	/**
	 * <p>Standard Constructor (with a default restartDelay of 1000ms, 
	 * a {@link CachePublisher.BruteForceConflictResolver} and an indefinite number
	 * of consecutive failures).</p>
	 * 
	 * @param cacheName
	 */
	public LocalCachePublisher(String cacheName) {
		this(cacheName, 1000);
	}
	
	
	/**
	 * <p>Returns the name of the cache to which we'll be publishing
	 * {@link EntryOperation}s.</p>
	 */
	public String getCacheName() {
		return cacheName;
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
	 * <p>Returns the {@link NamedCache} that we should use for
	 * executing {@link EntryOperation}s.</p>
	 */
	public NamedCache getNamedCache() {
		return CacheFactory.getCache(cacheName);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public ConflictResolver getConflictResolver() {
		return conflictResolver;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void publish(EntryOperation entryOperation) {
		try {
			NamedCache namedCache = getNamedCache();
			namedCache.invoke(entryOperation.getKey(), instantiateEntryOperationProcessor(entryOperation, getConflictResolver()));
			
		} catch (IllegalStateException illegalStateException) {
			CacheFactory.log(illegalStateException);
			throw illegalStateException;
			
		} catch (RuntimeException runtimeException) {
			CacheFactory.log(String.format("Failed to publish %s to Cache %s because of\n%s", entryOperation, cacheName, runtimeException), CacheFactory.LOG_ERR);
			throw new IllegalStateException(String.format("Attempted to publish to cache %s", cacheName));
		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		super.readExternal(in);
		this.cacheName = ExternalizableHelper.readSafeUTF(in);
		this.conflictResolver = (ConflictResolver)ExternalizableHelper.readObject(in);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		ExternalizableHelper.writeSafeUTF(out, cacheName);
		ExternalizableHelper.writeObject(out, conflictResolver);
	}


	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		super.readExternal(reader);
		this.cacheName = reader.readString(100);
		this.conflictResolver = (ConflictResolver)reader.readObject(101);
	}
	
	public EntryOperationProcessor instantiateEntryOperationProcessor(EntryOperation entryOperation, ConflictResolver conflictResolver) {
        return new EntryOperationProcessor(entryOperation, conflictResolver);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		super.writeExternal(writer);
		writer.writeString(100, cacheName);
		writer.writeObject(101, conflictResolver);
	}
	

	/**
	 * <p>An {@link EntryProcessor} that is performs the provided {@link EntryOperation}
	 * using the specified {@link CachePublisher.ConflictResolver}.</p>
	 */
	public static class EntryOperationProcessor extends AbstractProcessor implements ExternalizableLite, PortableObject {

		/**
		 * <p>The {@link EntryOperation} to perform.</p>
		 */
		protected EntryOperation entryOperation;
		
		
		/**
		 * <p>The {@link CachePublisher.ConflictResolver} that will resolve and perform
		 * the {@link EntryOperation}.</p>
		 */
		protected ConflictResolver conflictResolver;
		
		
		/**
		 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
		 */
		public EntryOperationProcessor() {
		}
		

		/**
		 * <p>Standard Constructor.</p>
		 * 
		 * @param entryOperation
		 * @param conflictResolver
		 */
		public EntryOperationProcessor(EntryOperation entryOperation, ConflictResolver conflictResolver) {
			this.entryOperation = entryOperation;
			this.conflictResolver = conflictResolver;
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public Object process(Entry entry) {
			conflictResolver.perform(entryOperation, entry);			
			return null;
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void readExternal(DataInput in) throws IOException {
			this.entryOperation = (EntryOperation)ExternalizableHelper.readObject(in);
			this.conflictResolver = (ConflictResolver)ExternalizableHelper.readObject(in);			
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void writeExternal(DataOutput out) throws IOException {
			ExternalizableHelper.writeObject(out, entryOperation);
			ExternalizableHelper.writeObject(out, conflictResolver);
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void readExternal(PofReader reader) throws IOException {
			this.entryOperation = (EntryOperation)reader.readObject(0);
			this.conflictResolver = (ConflictResolver)reader.readObject(1);
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void writeExternal(PofWriter writer) throws IOException {
			writer.writeObject(0, entryOperation);
			writer.writeObject(1, conflictResolver);
		}
	}
}
