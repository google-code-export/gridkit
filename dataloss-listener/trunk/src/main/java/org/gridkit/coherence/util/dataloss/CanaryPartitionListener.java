package org.gridkit.coherence.util.dataloss;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.PartitionedService;
import com.tangosol.net.partition.PartitionEvent;
import com.tangosol.net.partition.PartitionListener;

/**
 * Coherence partition listener for "canary" cache.
 * Checks if there are any lost values in the cache, which means in turn that some partition(s)
 * were lost during rebalancing.
 * 
 * @author malekseev
 * 05.04.2011
 */
public class CanaryPartitionListener implements PartitionListener {
	
	public static final String CACHE_NAME = "canary-cache";
	
	/**
	 * Checks cache state after particular transition events
	 */
	@Override
	public void onPartitionEvent(PartitionEvent partitionEvent) {
		final int eventType = partitionEvent.getId();
		
		switch (eventType) {
		case PartitionEvent.PARTITION_LOST: // really?
		case PartitionEvent.PARTITION_TRANSMIT_ROLLBACK:
		case PartitionEvent.PARTITION_TRANSMIT_COMMIT:
			
			NamedCache canaryCache = CacheFactory.getCache(CACHE_NAME);
			// FIXME move getAll(), keySet() out of the listener
			if (canaryCache.getAll(canaryCache.keySet()).size() != partitionEvent.getService().getPartitionCount()) {
				handlePartitionsLoss(canaryCache, partitionEvent.getService());
			}
			
		}
	}
	
	/**
	 * Handles partitions loss scenario. Typically, leads to cluster restart.
	 * May be overriden in subclasses.
	 * @param canaryCache canary cache
	 * @param service partitioned service
	 */
	protected void handlePartitionsLoss(NamedCache canaryCache, PartitionedService service) {
		StringBuilder sb = new StringBuilder();
		sb.append("Partitions loss detected after transition: lost partitions are ");
		// TODO calculate and add partitions list
		throw new IllegalStateException(sb.toString());
	}
	
}
