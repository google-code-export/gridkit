package com.griddynamics.gridkit.coherence.patterns.command.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DistributedCacheService;
import com.tangosol.net.NamedCache;
import com.tangosol.net.partition.KeyPartitioningStrategy;
import com.tangosol.net.partition.PartitionSet;
import com.tangosol.util.Base;

class OwnershipDaemon {
	
	private Map<String, ServiceContext> services;
	private Thread watchThread;
	
	public OwnershipDaemon() {
		watchThread = new Thread() {
			@Override
			public void run() {
				watch();
			}			
		};
		watchThread.setName("PartitionOwnershipDaemon");
		watchThread.setDaemon(true);		
		watchThread.start();
	}
	
	public void registerOwnershipListener(DistributedCacheService service, String cacheName, OwnershipListener listener) {
		synchronized (services) {
			ServiceContext ctx = services.get(service.getInfo().getServiceName());
			if (ctx == null) {
				ctx = new ServiceContext(service.getInfo().getServiceName(), CacheFactory.getCache(cacheName), service);
				services.put(ctx.serviceName, ctx);
			}
			
			ctx.addListener(cacheName, listener);
			services.notify();
		}
	}
	
	private void watch() {
		try {
			while(true) {
				List<ServiceContext> contexts;
				synchronized(services) {
					if (services.isEmpty()) {
						services.wait();
						continue;
					}
					else {
						contexts = new ArrayList<ServiceContext>(services.values());
					}
				}
				
				for(ServiceContext ctx: contexts) {
					ctx.updatePartionLocks();
				}
				
				services.wait(10); // 10 ms interval for partition checks
			}
		} catch (InterruptedException e) {
			Base.err(e);
			Base.err("Ownership daemon has failed");
		}
	}
	
	public interface OwnershipListener {
		public void partitionsOwnershipChanged(DistributedCacheService service, String cacheName, PartitionSet withdrawn, PartitionSet assigned);		
	}
	
	private static class ServiceContext {

		private final String serviceName;
		private final DistributedCacheService service;
		private final NamedCache lockCache;
		private PartitionSet lockedPartitions;
		
		private final Map<OwnershipListener, String> listeners;
		
		public ServiceContext(String serviceName, NamedCache cache, DistributedCacheService service) {
			this.serviceName = serviceName;
			this.service = service;
			this.lockCache = cache;
			this.listeners = new HashMap<OwnershipListener, String>();
		}
		
		public synchronized void addListener(String cache, OwnershipListener listener) {
			listeners.put(listener, cache);
			if (lockedPartitions != null) {
				listener.partitionsOwnershipChanged(service, cache, null, lockedPartitions);
			}
		}

		public synchronized void removeListener(OwnershipListener listener) {
			String cache = listeners.remove(listener);
			if (lockedPartitions != null && cache != null) {
				listener.partitionsOwnershipChanged(service, cache, lockedPartitions, null);
			}
		}
		
		public synchronized void updatePartionLocks() {
			
			if (lockedPartitions == null) {
				lockedPartitions = new PartitionSet(service.getPartitionCount());
			}
			
			PartitionSet set = service.getOwnedPartitions(CacheFactory.getCluster().getLocalMember());
			PartitionSet withdrawn = null;
			PartitionSet assigned = null;
			
			if (!set.contains(lockedPartitions)) {
				withdrawn = new PartitionSet(lockedPartitions);
				withdrawn.remove(set);
			}
			if (!lockedPartitions.contains(set)) {
				assigned = new PartitionSet(set);
				assigned.remove(lockedPartitions);
			}			
			if (assigned != null) {
				lock(assigned);
			}
			notify(withdrawn, assigned);			
			if (withdrawn != null) {
				unlock(withdrawn);				
			}
		}
		
		private void notify(PartitionSet withdrawn, PartitionSet assigned) {
			for(Map.Entry<OwnershipListener, String> entry: listeners.entrySet()) {
				try {
					entry.getKey().partitionsOwnershipChanged(service, entry.getValue(), withdrawn, assigned);
				}
				catch(Exception e) {
					Base.err(e);
				}
			}
		}

		void lock(PartitionSet assigned) {
			Collection<PartitionKey> keys = toKeys(assigned);
			for(PartitionKey key: keys) {
				lockCache.lock(key); // TODO dead lock prevention
			}
			lockedPartitions.add(assigned);
		}

		void unlock(PartitionSet assigned) {
			Collection<PartitionKey> keys = toKeys(assigned);
			for(PartitionKey key: keys) {
				lockCache.unlock(key); // TODO dead lock prevention
			}
			lockedPartitions.remove(assigned);
		}

		Collection<PartitionKey> toKeys(PartitionSet set) {
			KeyPartitioningStrategy kps = service.getKeyPartitioningStrategy();
			set = new PartitionSet(set);
			List<PartitionKey> keys = new ArrayList<PartitionKey>(set.cardinality());
			for(int i = 0; !set.isEmpty(); ++i) {
				PartitionKey key = new PartitionKey(i);
				int pId = kps.getKeyPartition(key);
				if (set.contains(pId)) {
					keys.add(key);
					set.remove(pId);
				}
			}
			return keys;
		}
	}
}
