package org.gridkit.coherence.util.dataloss;

import java.util.HashMap;
import java.util.Map;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.PartitionedService;

/**
 * Cache population util for "canary" cache. 
 * To be called from any storage-enabled Coherence cluster nodes.
 * 
 * @author malekseev
 * 05.04.2011
 */
public class CanaryCachePopulator {
	
	public static void populate() {
		CacheFactory.ensureCluster();
		NamedCache canaryCache = CacheFactory.getCache(CanaryPartitionListener.CACHE_NAME);
		PartitionedService partitionedService = (PartitionedService) canaryCache.getCacheService();
		
		int partitionsCount = partitionedService.getPartitionCount();
		
		Map<Integer, Object> data = new HashMap<Integer, Object>(partitionsCount);
		for (int i = 0; i < partitionsCount; i++) data.put(i, (byte) 1);
		
		// ack cache lock for partition 0
		Object lockKey = 0;
		canaryCache.lock(lockKey, -1);
		try {
			if (canaryCache.size() == 0) {
				canaryCache.putAll(data);
			}
		} catch(Exception e) {
			// FIXME slf4j or template method pattern?
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			// rel cache lock
			canaryCache.unlock(lockKey);
		}
	}
	
}
