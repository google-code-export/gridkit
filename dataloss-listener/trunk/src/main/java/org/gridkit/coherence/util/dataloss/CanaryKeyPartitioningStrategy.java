package org.gridkit.coherence.util.dataloss;

import com.tangosol.net.partition.DefaultKeyPartitioningStrategy;

/**
 * Coherence key partitioning strategy for "canary" cache.
 * Distributes cache keys among all scheme partitions.
 * 
 * @author malekseev
 * 05.04.2011
 */
public class CanaryKeyPartitioningStrategy extends DefaultKeyPartitioningStrategy {

	/**
	 * Returns partition number equal to the key value, assuming that key in an Integer value.
	 * Key association logic is skipped for this strategy, since no association may be applied
	 * to "canary" cache.
	 */
	@Override
	public int getKeyPartition(Object oKey) {
		int part = ((Integer) oKey).intValue(); 
		if (part < m_service.getPartitionCount()) {
			return part;
		} else {
			throw new IllegalStateException(
					"Trying to store partition key with value greater or equal than partitions count");
		}
	}

}
