/*
 * File: RemoteCachePublisher.java
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
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.CacheService;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Service;
import com.tangosol.net.cache.WrapperNamedCache;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.NullImplementation;

/**
 * <p>A {@link CachePublisher} based on the {@link LocalCachePublisher} that uses 
 * a <strong>remote</strong> {@link NamedCache} in which to publish {@link EntryOperation}s.</p>
 * 
 * <p>NOTE: Although this implementation will work successfully as 
 * a means to publish to remote caches, due to the lack of "batch"
 * support, this implementation <strong>may not be very effective<strong> when 
 * large volumes of updates are occurring across a high-latency network
 * (ie: a WAN).  Instead of using the {@link RemoteCachePublisher}, 
 * we <strong>strongly recommend</strong> that you look to use the 
 * {@link RemoteInvocationPublisher}, that
 * supports batching and significantly reduces network round-trips
 * (only ever 1 round trip per batch, regardless of batch size).</p>
 *
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class RemoteCachePublisher extends LocalCachePublisher {
	
	/**
	 * <p>The name of the remote {@link CacheService} that 
	 * we'll use to attach to the remote cache.</p> 
	 */
	private String remoteCacheServiceName;
	
	
	/**
	 * <p>The remote {@link NamedCache} that we're using to perform {@link EntryOperation}s.</p>
	 */
	private transient NamedCache remoteCache;

	
	/**
	 * <p>A simple constant to represent a remote {@link NamedCache} that is offline.</p>
	 */
	protected final static transient NamedCache OFFLINE_CACHE = new WrapperNamedCache(NullImplementation.getMap(), null) {
		public boolean isActive() {
			return false;
		}
	};

	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public RemoteCachePublisher() {
	}

	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param remoteCacheServiceName
	 * @param remoteCacheName
	 * @param conflictResolver
	 * @param restartDelay
	 * @param totalConsecutiveFailuresBeforeSuspending
	 */
	public RemoteCachePublisher(String remoteCacheServiceName,
								String remoteCacheName,
								ConflictResolver conflictResolver,
								long restartDelay,
								int totalConsecutiveFailuresBeforeSuspending) {
		super(remoteCacheName, conflictResolver, restartDelay, totalConsecutiveFailuresBeforeSuspending);
		this.remoteCacheServiceName = remoteCacheServiceName;
		this.remoteCache = OFFLINE_CACHE;
	}
	
	
	/**
	 * <p>Standard Constructor (that uses a {@link CachePublisher.BruteForceConflictResolver}
	 * and supports an indefinite number of consecutive failures).</p>
	 * 
	 * @param remoteCacheServiceName
	 * @param remoteCacheName
	 * @param restartDelay
	 */
	public RemoteCachePublisher(String remoteCacheServiceName,
								String remoteCacheName,
								long restartDelay
							    ) {
		super(remoteCacheName, restartDelay);
		this.remoteCacheServiceName = remoteCacheServiceName;
		this.remoteCache = OFFLINE_CACHE;
	}

	
	/**
	 * <p>Returns the name of the remote {@link CacheService} that we'll use
	 * to perform {@link EntryOperation}s.</p>
	 */
	public String getRemoteCacheServiceName() {
		return remoteCacheServiceName;
	}
		
	
	/**
	 * {@inheritDoc}
	 */
	public void start(SubscriptionIdentifier subscriptionIdentifier) {
		try {
			Service service = CacheFactory.getConfigurableCacheFactory().ensureService(remoteCacheServiceName);
	
	        if (CacheService.TYPE_REMOTE.equals(service.getInfo().getServiceType())) {
	        	
	        	remoteCache = ((CacheService)service).ensureCache(getCacheName(), Thread.currentThread().getContextClassLoader());
	        	
				if (CacheFactory.isLogEnabled(CacheFactory.LOG_DEBUG))
					CacheFactory.log(String.format("Connected to RemoteCache %s using the RemoteCacheService %s", getCacheName(), remoteCacheServiceName), CacheFactory.LOG_DEBUG);
	        	
	        } else {
	            throw new IllegalArgumentException("The Service " + remoteCacheServiceName + " is not a remote cache service");
	        }
	        
		} catch (RuntimeException runtimeException) {
			
			CacheFactory.log(String.format("Failed to connect to RemoteCache %s using the RemoteCacheService %s", getCacheName(), remoteCacheServiceName), CacheFactory.LOG_WARN);        	
			remoteCache = OFFLINE_CACHE;
			throw runtimeException;
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void stop(SubscriptionIdentifier subscriptionIdentifier) {
		if (remoteCache != OFFLINE_CACHE) {

			if (CacheFactory.isLogEnabled(CacheFactory.LOG_DEBUG))
				CacheFactory.log(String.format("Disconnected from RemoteCache %s using the RemoteCacheService %s", getCacheName(), remoteCacheServiceName), CacheFactory.LOG_DEBUG);

			remoteCache.destroy();
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */	
	@Override
	public NamedCache getNamedCache() {
		if (remoteCache == OFFLINE_CACHE) {
			throw new IllegalStateException(String.format("Remote cache %s is currently offline", getCacheName()));
		} else {
			return remoteCache;
		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		super.readExternal(in);
		this.remoteCacheServiceName = ExternalizableHelper.readSafeUTF(in);
	}


	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		ExternalizableHelper.writeSafeUTF(out, remoteCacheServiceName);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		super.readExternal(reader);
		this.remoteCacheServiceName = reader.readString(200);
	}


	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		super.writeExternal(writer);
		writer.writeString(200, remoteCacheServiceName);
	}
}
