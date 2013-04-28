/**
 * Copyright 2013 Alexey Ragozin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gridkit.coherence.cachewatchdog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tangosol.net.CacheService;
import com.tangosol.net.Cluster;
import com.tangosol.net.DistributedCacheService;
import com.tangosol.net.Member;
import com.tangosol.net.MemberEvent;
import com.tangosol.net.MemberListener;
import com.tangosol.net.NamedCache;
import com.tangosol.net.RequestPolicyException;
import com.tangosol.net.partition.PartitionSet;
import com.tangosol.net.partition.SimplePartitionKey;
import com.tangosol.util.CompositeKey;
import com.tangosol.util.Filter;
import com.tangosol.util.extractor.IdentityExtractor;
import com.tangosol.util.filter.AlwaysFilter;
import com.tangosol.util.filter.EqualsFilter;
import com.tangosol.util.processor.ConditionalPut;
import com.tangosol.util.processor.ConditionalRemove;

/**
 * <p>
 * {@link PartitionMonitor} helps to track data consistency in Coherence cache.
 * Each partition in cache has associated consistency flag (canary key).
 * <br/>
 * In partition without such flag is present {@link PartitionMonitor} will invoke user provided {@link PartitionListener}
 * which is supposed to preload data in partition. Once {@link PartitionListener} for partition is executed, consitency flag would be set.
 * <br/>
 * Consistency flag cloud be reset again if data would be lost do to failure of cluster storage node.
 * <br/>
 * {@link PartitionMonitor} is using distributed locks to ensure what each partition would be preloaded exectly once.
 * Multiple instances of {@link PartitionMonitor} cloud work concurrently.
 * <br/>
 * {@link PartitionMonitor} is encapsulating a thread pool, so it is recommended to have single instance {@link PartitionMonitor} per JVM.
 * </p>
 * <p>
 * {@link PartitionMonitor} requires following elements in {@code cache-config.xml}
 * <br/>
 * In {@code <caching-scheme-mapping>} add element
 * <pre>
 * {@code
 * <cache-mapping>
 *     <!-- 
 *       This is special configuration element for data loss monitor.
 *       Do not modify.
 *      -->
 *     <cache-name>CANARY_CACHE</cache-name>
 *     <scheme-name>CANARY_SCHEME</scheme-name>
 * </cache-mapping>}
 * </pre>
 * In {@code <caching-schemes>} add element
 * <pre>
 * {@code
 * <distributed-scheme>
 *     <!-- 
 *         This is special configuration element for data loss monitor.
 *         Do not modify.
 *      -->
 *     <scheme-name>CANARY_SCHEME</scheme-name>
 *     <backing-map-scheme>
 *         <local-scheme/>
 *     </backing-map-scheme>
 * </distributed-scheme>}
 * </pre>
 * </p>
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class PartitionMonitor {

	private final static Logger LOGGER = LoggerFactory.getLogger(PartitionMonitor.class);
	
	private String canaryCacheName = "CANARY_CACHE";
	private ExecutorService workerPool;
	private Cluster cluster;
	
	private Map<String, ServiceContext> monitoredServices = new ConcurrentHashMap<String, ServiceContext>();
	
	private boolean terminated = false;
	private MembershipListener memberListener = new MembershipListener();
	
	public PartitionMonitor() {
		this(16);
	}

	public PartitionMonitor(int poolSize) {
		workerPool = new ThreadPoolExecutor(poolSize, poolSize, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread th = new Thread(r);
				th.setDaemon(true);
				return th;
			}
		});
	}

	/**
	 * @see {@link #attachPartitionMonitor(NamedCache, PartitionListener, int, int)}
	 */
	public void attachPartitionMonitor(NamedCache cache, PartitionListener monitor) {
		attachPartitionMonitor(cache, monitor, Integer.MAX_VALUE);
	}

	/**
	 * @param maxPartitionsPerCall - will limit number of partition per call to {@link PartitionListener#onEmptyPartition(NamedCache, PartitionSet)} method
	 * @see {@link #attachPartitionMonitor(NamedCache, PartitionListener, int, int)}
	 */
	public void attachPartitionMonitor(NamedCache cache, PartitionListener monitor, int maxPartitionsPerCall) {
		attachPartitionMonitor(cache, monitor, maxPartitionsPerCall, Integer.MAX_VALUE);
	}
	
	/**
	 * @param maxPartitionsPerCall - will limit number of partition per call to {@link PartitionListener#onEmptyPartition(NamedCache, PartitionSet)} method
	 * @param concurrency - max number of listener invocation, which cloud be made in parallel
	 */
	public synchronized void attachPartitionMonitor(NamedCache cache, PartitionListener monitor, int maxPartitionsPerCall, int concurrency) {
		if (maxPartitionsPerCall <= 0) {
			throw new IllegalArgumentException("concurency should be positive (" + maxPartitionsPerCall + ")");
		}
		if (concurrency <= 0) {
			throw new IllegalArgumentException("concurency should be positive (" + concurrency + ")");
		}
		CacheService service = cache.getCacheService();
		if (cluster == null) {
			cluster = service.getCluster();
		}
		if (service instanceof DistributedCacheService) {
			DistributedCacheService dservice = (DistributedCacheService) service;
			String sname = dservice.getInfo().getServiceName();
			ServiceContext ctx = monitoredServices.get(sname);
			if (ctx == null) {
				ctx = newContext(dservice);
				service.addMemberListener(memberListener);
				monitoredServices.put(sname, ctx);
			}
			
			String cacheName = cache.getCacheName();
			
			if (ctx.caches.containsKey(cacheName)) {
				throw new IllegalArgumentException("Monitor is already registered for cache '" + cacheName + "'");
			}			
			
			CacheContext cctx = new CacheContext(ctx, cache, monitor, maxPartitionsPerCall, concurrency);
			ctx.caches.put(cacheName, cctx);
			
			updateService(dservice);
		}
		else {
			throw new IllegalArgumentException("May work only with distributed-scheme. Cannot be used from Extend clients");
		}						
	}

	private ServiceContext newContext(DistributedCacheService dservice) {
		NamedCache canaryCache = dservice.ensureCache(canaryCacheName, null);				
		return new ServiceContext(dservice, canaryCache);
	}

	public synchronized void removePartitionMonitor(NamedCache cache) {
		CacheService service = cache.getCacheService();
		if (service instanceof DistributedCacheService) {
			DistributedCacheService dservice = (DistributedCacheService) service;
			String sname = dservice.getInfo().getServiceName();
			ServiceContext ctx = monitoredServices.get(sname);
			if (ctx == null) {
				throw new IllegalArgumentException("No monitor installed for cache '" + cache.getCacheName() + "'");
			}
			
			String cacheName = cache.getCacheName();
			
			if (!ctx.caches.containsKey(cacheName)) {
				throw new IllegalArgumentException("No monitor installed for cache '" + cache.getCacheName() + "'");
			}			
			
			ctx.caches.remove(cacheName);
			
			if (ctx.caches.isEmpty()) {
				monitoredServices.remove(sname);
				dservice.removeMemberListener(memberListener);
			}
			
			updateService(dservice);
		}
		else {
			throw new IllegalArgumentException("May work only with distributed-scheme. Cannot be used from Extend clients");
		}						
	}
	
	@SuppressWarnings("unchecked")
	public PartitionSet getEmptyPartitions(NamedCache cache) {
		CacheService service = cache.getCacheService();
		if (service instanceof DistributedCacheService) {
			DistributedCacheService dservice = (DistributedCacheService) service;
			int pcount = dservice.getPartitionCount();
			NamedCache canaryCache = dservice.ensureCache(canaryCacheName, null);
			Set<CompositeKey> ckeys = new HashSet<CompositeKey>();
			String cacheName = cache.getCacheName();
			for(int i = 0; i != pcount; ++i) {
				ckeys.add(canaryKey(cacheName, i));
			}
			ckeys = canaryCache.getAll(ckeys).keySet();
			PartitionSet missing = new PartitionSet(pcount);
			missing.invert();
			for(CompositeKey key: ckeys) {
				missing.remove(((SimplePartitionKey)key.getPrimaryKey()).getPartitionId());
			}
			return missing;
		}
		else {
			throw new IllegalArgumentException("May work only with distributed-scheme. Cannot be used from Extend clients");
		}		
	}

	private CompositeKey canaryKey(String cacheName, int p) {
		return new CompositeKey(SimplePartitionKey.getPartitionKey(p), cacheName);
	}

	public void updateService(DistributedCacheService service) {
		final ServiceContext ctx = monitoredServices.get(service.getInfo().getServiceName());
		if (ctx != null) {
			workerPool.submit(new Runnable() {
				@Override
				public void run() {
					try {
						checkService(ctx);
					}
					catch(Throwable e) {
						LOGGER.error("Error in DataLossMonitor for service [" + ctx.serviceName + "]", e);
					}
				}
			});
		}
	}
	
	private void checkService(ServiceContext ctx) {
		Thread currentThread = Thread.currentThread();
		String origThreadName = currentThread.getName();
		try {
			currentThread.setName("DataLossMonitor-CheckService[" + ctx.service.getInfo().getServiceName() + "]");
			ctx.signal.release(1);
			if (ctx.lock.tryLock()) {
				try {
					while(ctx.signal.drainPermits() > 0) {
						performServiceCheck(ctx);
					}
				}
				finally {
					ctx.lock.unlock();
				}
			}
		}
		finally {
			currentThread.setName(origThreadName);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void performServiceCheck(ServiceContext ctx) {
		try {
			List<CacheContext> caches;
			synchronized(this) {
				caches = new ArrayList<CacheContext>(ctx.caches.values());
			}
			Set<CompositeKey> ckeys = new HashSet<CompositeKey>();
			ckeys.addAll(ctx.canaryCache.keySet());
			for(CacheContext context: caches) {
				PartitionSet missing = getEmptyPartitions(ctx.service.getPartitionCount(), ckeys, context.cacheName);
				missing.remove(context.localLocks);
				if (!missing.isEmpty()) {
					checkCache(context);
				}
			}
			ctx.deadCache.clear();
		}
		catch(RequestPolicyException e) {
			// service is not online
			LOGGER.warn("Service is not online [" + ctx.serviceName + "]");			
		}
	}

	private PartitionSet getEmptyPartitions(int partitionCount, Set<CompositeKey>ckeys, String cacheName) {
		PartitionSet missing = new PartitionSet(partitionCount);
		missing.invert();
		for(CompositeKey ckey: ckeys) {
			if (cacheName.equals(ckey.getSecondaryKey())) {
				SimplePartitionKey pk = (SimplePartitionKey) ckey.getPrimaryKey();
				missing.remove(pk.getPartitionId());
			}
		}
		return missing;
	}

	private void checkCache(CacheContext ctx) {
		Thread currentThread = Thread.currentThread();
		String origThreadName = currentThread.getName();
		try {
			currentThread.setName("DataLossMonitor-CheckCache[" + ctx.id + "]");
			ctx.signal.release(1);
			if (ctx.lock.tryLock()) {
				try {
					while(ctx.signal.drainPermits() > 0) {
						performCheckCache(ctx);
					}
				}
				finally {
					ctx.lock.unlock();
				}
			}
		}
		finally {
			currentThread.setName(origThreadName);
		}
	}
	
	private void performCheckCache(final CacheContext ctx) {	
		PartitionSet missing = getEmptyPartitions(ctx.cache);
		missing.remove(ctx.localLocks);
		while(!missing.isEmpty()) {
			if (!ctx.permits.tryAcquire()) {
				// concurrency threshold has been reached
				return;
			}
			final PartitionSet batch = new PartitionSet(missing.getPartitionCount());
			while(batch.cardinality() < ctx.batchLimit && !missing.isEmpty()) {
				int p = missing.next(0);
				missing.remove(p);
				if (lockCanary(ctx, p)) {
					batch.add(p);
				}
			}
			if (!batch.isEmpty()) {
				lockLocally(ctx, batch);
				workerPool.submit(new Runnable() {
					@Override
					public void run() {
						processBatch(ctx, batch);
					}
				});
			}
			else {
				ctx.permits.release();
			}
		}
	}

	private static Filter isNull() {
		return new EqualsFilter(new IdentityExtractor(), null);
	}
	
	private boolean lockCanary(CacheContext ctx, int p) {
		CompositeKey key = new CompositeKey(SimplePartitionKey.getPartitionKey(p), new CompositeKey(ctx.cacheName, "lock"));
		while(true) {
			String uid = (String) ctx.canaryCache.invoke(key, new ConditionalPut(isNull(), getLocalUID(), true));		
			if (uid != null) {
				if (getLocalUID().equals(uid) || !isActive(ctx.service, uid)) {
					if (!getLocalUID().equals(uid) && !ctx.deadCache.contains(uid)) {
						ctx.deadCache.add(uid);
						LOGGER.warn("Removing dead member lock: " + uid);
					}
					ctx.canaryCache.invoke(key, new ConditionalRemove(new EqualsFilter(new IdentityExtractor(), uid)));
					continue;
				}
				else {
					return false;
				}
			}
			return true;
		}		
	}
	
	private void addCanaries(CacheContext ctx, PartitionSet set) {
		Set<Object> keys = new HashSet<Object>();
		for(int p: set.toArray()) {
			CompositeKey key = new CompositeKey(SimplePartitionKey.getPartitionKey(p), ctx.cacheName);
			keys.add(key);
		}
		ctx.canaryCache.invokeAll(keys, new ConditionalPut(AlwaysFilter.INSTANCE, Boolean.TRUE));		
	}

	private void unlockCanaries(CacheContext ctx, PartitionSet set) {
		Set<Object> keys = new HashSet<Object>();
		for(int p: set.toArray()) {
			CompositeKey key = new CompositeKey(SimplePartitionKey.getPartitionKey(p), new CompositeKey(ctx.cacheName, "lock"));
			keys.add(key);
		}
		ctx.canaryCache.invokeAll(keys, new ConditionalRemove(new EqualsFilter(new IdentityExtractor(), getLocalUID())));		
	}

	private String getLocalUID() {
		return getUID(cluster.getLocalMember());
	}

	private boolean isActive(DistributedCacheService service, String uid) {
		@SuppressWarnings("unchecked")
		Set<Member> members = service.getInfo().getServiceMembers();
		for(Member m: members) {
			if (uid.equals(getUID(m))) {
				return true;
			}
		}
		return false;
	}

	private String getUID(Member m) {
		return m.getRoleName() + "-" + m.getProcessName() + "-" + m.getUid().toString();
	}

	protected void processBatch(CacheContext ctx, PartitionSet batch) {
		Thread currentThread = Thread.currentThread();
		String origThreadName = currentThread.getName();
		try {
			currentThread.setName("DataLossMonitor-Process[" + ctx.id + ", " + batch + "]");
			try {
				PartitionSet ckeys = getEmptyPartitions(ctx.cache);
				PartitionSet finalBatch = new PartitionSet(batch);
				finalBatch.retain(ckeys);
				if (!finalBatch.isEmpty()) {
					ctx.monitor.onEmptyPartition(ctx.cache, new PartitionSet(finalBatch));
				}
				addCanaries(ctx, batch);
			}			
			finally {
				ctx.permits.release();
				unlockCanaries(ctx, batch);
				unlockLocally(ctx, batch);
				// verify cache state
				updateService((DistributedCacheService) ctx.cache.getCacheService());
			}
		}
		catch(Exception e) {
			LOGGER.warn("Execption in listener for cache [" + ctx.cacheName + "]", e);
		}
		finally {
			currentThread.setName(origThreadName);
		}		
	}

	private void lockLocally(CacheContext ctx, PartitionSet batch) {
		synchronized (ctx) {
			PartitionSet nlock = new PartitionSet(ctx.localLocks);
			nlock.add(batch);				
			ctx.localLocks = nlock;
		}
	}

	private void unlockLocally(CacheContext ctx, PartitionSet batch) {
		synchronized (ctx) {
			PartitionSet nlock = new PartitionSet(ctx.localLocks);
			nlock.remove(batch);				
			ctx.localLocks = nlock;
		}
	}

	public static interface PartitionListener {
		
		/**
		 * This method is called once one or more empty partitions in cache have been detected.
		 * After execution of this method. Partition fill be automatically marked as initialized.
		 * 
		 * @param cache
		 * @param partitions - set of not initialized partitions
		 */
		public void onEmptyPartition(NamedCache cache, PartitionSet partitions);
		
	}
	
	private class MembershipListener implements MemberListener {

		@Override
		public void memberJoined(MemberEvent member) {
			if (!terminated) {
				updateService((DistributedCacheService) member.getService());
			}
		}

		@Override
		public void memberLeaving(MemberEvent member) {
			// ignore
		}

		@Override
		public void memberLeft(MemberEvent member) {
			if (!terminated) {
				updateService((DistributedCacheService) member.getService());
			}
		}
	}
	
	private static class ServiceContext {
		
		final DistributedCacheService service;
		final String serviceName;
		final Semaphore signal = new Semaphore(0);
		final Lock lock = new ReentrantLock();
		final Map<String, CacheContext> caches = new HashMap<String, CacheContext>();
		final NamedCache canaryCache;
		final Set<String> deadCache = Collections.synchronizedSet(new HashSet<String>());

		public ServiceContext(DistributedCacheService service,	NamedCache canaryCache) {
			this.service = service;
			this.serviceName = service.getInfo().getServiceName();
			this.canaryCache = canaryCache;
		}
	}
	
	private static class CacheContext {
		
		final String cacheName;
		final CompositeKey id;
		final NamedCache cache;
		final Semaphore signal = new Semaphore(0);
		final Lock lock = new ReentrantLock();
		
		final DistributedCacheService service;
		final NamedCache canaryCache;
		final PartitionListener monitor;
		final int batchLimit;		
		
		volatile PartitionSet localLocks;
		final Set<String> deadCache;
		final Semaphore permits;
		
		
		public CacheContext(ServiceContext ctx, NamedCache cache, PartitionListener monitor, int batchLimit, int concurency) {
			cacheName = cache.getCacheName();
			this.cache = cache;
			service = ctx.service;
			canaryCache = ctx.canaryCache;
			id = new CompositeKey(service.getInfo().getServiceName(), cacheName);
			this.monitor = monitor;
			this.batchLimit = batchLimit;
			
			localLocks = new PartitionSet(service.getPartitionCount());
			deadCache = ctx.deadCache;
			permits = new Semaphore(concurency);
		}
	}
}
