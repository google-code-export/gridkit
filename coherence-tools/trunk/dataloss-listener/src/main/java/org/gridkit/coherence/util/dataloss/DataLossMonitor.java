/**
 * Copyright 2011 Grid Dynamics Consulting Services, Inc.
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
package org.gridkit.coherence.util.dataloss;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.PartitionedService;
import com.tangosol.net.partition.KeyPartitioningStrategy;

/**
 * Holds all "canary" cache maintenance issues. 
 * Processes cache population, consistency checks, restores in an async executor thread.
 * 
 * @author malekseev
 * 06.04.2011
 */
class DataLossMonitor {
	
	private static final Logger logger = LoggerFactory.getLogger(DataLossMonitor.class);
	
	private final DataLossListener listener;
	private final String cacheName;
	private final Lock checkLock;
	private final ExecutorService executor;
	private final Future<Map<Integer, Integer>> wrappedPartitionsMap;
	
	public DataLossMonitor(String cacheName, DataLossListener listener) {
		this.listener = listener;
		this.cacheName = cacheName;
		this.checkLock = new ReentrantLock();
		this.executor = Executors.newSingleThreadExecutor();
		try {
			wrappedPartitionsMap = executor.submit(new CachePopulator());
			executor.submit(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					checkLock.lock();
					return null;
				}
			});
			new Thread(new ConsistencyValidator(), "Consistency validator" + cacheName).start();
		} catch (Exception e) {
			logger.error("Exception during \"canary\" cache init.", e);
			throw new RuntimeException(e);
		}
	}
	
	public void checkConsistency() {
		logger.trace("Posting consistency check request...");
		executor.execute(new Runnable() {
			@Override
			public void run() {
				checkLock.unlock();
				checkLock.lock();
			}
		});
	}
	
	
	
	
	/**
	 * Initially populates "canary" cache with data. 
	 * Cache consists of integer pairs   
	 * 		some guessed key -> partition no
	 * 
	 * Callable returns partitions map (partition no -> "canary" key), i.e. reverse canary cache, 
	 * for future restores after partitions loss. 
	 */
	private class CachePopulator implements Callable<Map<Integer, Integer>> {
		
		private static final int LOOP_LIMIT_PER_PARTITION = 10000;
		
		private final Logger logger = LoggerFactory.getLogger(CachePopulator.class);
		
		private Map<Integer, Integer> cacheMap;
		private Map<Integer, Integer> partitionsMap;
		
		@Override
		public Map<Integer, Integer> call() throws Exception {
			CacheFactory.ensureCluster();
			
			NamedCache canaryCache = CacheFactory.getCache(cacheName);
			PartitionedService partitionedService = (PartitionedService) canaryCache.getCacheService();
			
			int partitionsCount = partitionedService.getPartitionCount();
			
			cacheMap = new HashMap<Integer, Integer>(partitionsCount);
			partitionsMap = new HashMap<Integer, Integer>(partitionsCount);
			
			KeyPartitioningStrategy kps = partitionedService.getKeyPartitioningStrategy();
			
			long start = System.nanoTime();
			for (int i = 0; i < partitionsCount * LOOP_LIMIT_PER_PARTITION; i++) {
				int partitionNo = kps.getKeyPartition(i);
				if (!partitionsMap.containsKey(partitionNo)) {
					partitionsMap.put(partitionNo, i);
					cacheMap.put(i, partitionNo);
					if (partitionsMap.size() == partitionsCount) break; /* exit for */
				}
			}
			long elapsed = System.nanoTime() - start;
			
			if (partitionsMap.size() < partitionsCount) {
				StringBuilder sb = new StringBuilder("Unable to fill \"canary\" cache partitions with sample data: parts no ");
				for (int i = 0; i < partitionsCount; i++) {
					if (!partitionsMap.containsKey(i)) {
						sb.append(i);
						sb.append(' ');
					}
				}
				sb.append("for service ");
				sb.append(partitionedService.getInfo().getServiceName());
				logger.error(sb.toString());
				throw new IllegalStateException("Canary cache construction failed");
			}
			
			Object lockKey = 0;
			canaryCache.lock(lockKey, -1);
			try {
				if (canaryCache.size() == 0) {
					canaryCache.putAll(cacheMap);
					logger.info("Canary cache for service '{}' filled successfully in {} ms", 
							partitionedService.getInfo().getServiceName(), 
							MILLISECONDS.convert(elapsed, NANOSECONDS));
				}
			} catch(Exception e) {
				logger.error("Exception during \"canary\" cache population.", e);
				throw new RuntimeException(e);
			} finally {
				canaryCache.unlock(lockKey);
			}
			
			return partitionsMap;
		}

	}
	
	
	/**
	 * Checks if there are some lost entries in "canary" cache, indication lost Coherence 
	 * partitions.
	 * 
	 * When found, delegates processing to application-provided listener class, then 
	 * restores "canary" cache contents using previously generated partitions map.
	 */
	private class ConsistencyValidator implements Runnable {
		
		private final Logger logger = LoggerFactory.getLogger(ConsistencyValidator.class);
		
		@Override
		public void run() {
			final NamedCache canaryCache = CacheFactory.getCache(cacheName);
			final PartitionedService service = (PartitionedService) canaryCache.getCacheService();
			
			while (true) {
				try {
					checkLock.lockInterruptibly();
					checkLock.unlock();
				} catch (InterruptedException e) {
					
				}
				
				logger.trace("Processing consistency check request...");
				
				@SuppressWarnings("unchecked")
				// canaryCache.size() may work too, but we'll stick with getAll() to be absolutely sure
				Map<Integer, Integer> cacheMap = canaryCache.getAll(canaryCache.keySet());
				if (cacheMap.size() != service.getPartitionCount()) {
					// write log message
					logger.error(lossMessage(service, cacheMap));
					
					// calculate lost partitions list
					int lostCount = 0, lostPartitions[] = new int[service.getPartitionCount()];
					for (int i = 0; i < service.getPartitionCount(); i++) {
						if (!cacheMap.containsValue(i)) {
							lostPartitions[lostCount] = i;
							lostCount++;
						}
					}
					
					// delegate to application-provided listener
					listener.onPartitionLost(service, Arrays.copyOf(lostPartitions, lostCount));
					
					// recover lost "canary" cache partitions
					try {
						Map<Integer, Integer> partitionsMap = wrappedPartitionsMap.get();
						Map<Integer, Integer> lostCanaryPart = new HashMap<Integer, Integer>(lostCount);
						for (int i = 0; i < lostCount; i++) {
							lostCanaryPart.put(partitionsMap.get(lostPartitions[i]), lostPartitions[i]);
						}
						canaryCache.putAll(lostCanaryPart);
						logger.info("Canary cache partitions for service '{}' restored successfully", 
								service.getInfo().getServiceName());
					} catch (InterruptedException e) {
						logger.error("Exception during \"canary\" cache recovery", e);
						throw new RuntimeException(e);
					} catch (ExecutionException e) {
						logger.error("Exception during \"canary\" cache recovery", e);
						throw new RuntimeException(e);
					}
				}
			}
		}
		
		private String lossMessage(PartitionedService service, Map<Integer, Integer> cacheMap) {
			StringBuilder sb = new StringBuilder();
			sb.append("Detected loss of ");
			sb.append(service.getPartitionCount() - cacheMap.size());
			sb.append(" partitions for service '");
			sb.append(service.getInfo().getServiceName());
			sb.append("', delegating to application-provided listener");
			return sb.toString();
		}
		
	}
	
}
