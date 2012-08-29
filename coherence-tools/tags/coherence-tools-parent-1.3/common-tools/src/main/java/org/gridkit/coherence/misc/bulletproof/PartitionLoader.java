package org.gridkit.coherence.misc.bulletproof;

import java.util.Collection;

import com.tangosol.net.cache.BinaryEntryStore;
import com.tangosol.net.cache.CacheLoader;

/**
 * Mix-in interface to add bulk partition loading to {@link CacheLoader} or {@link BinaryEntryStore}. 
 * 
 * @author Alexey Ragozin (alexey.ragozin@db.com)
 */
public interface PartitionLoader {
	
	/** 
	 * This method should return all keys falling into specified partition from persistent storage.
	 * {@link CacheLoader} interface will be used to load actual data for keys.  
	 * 
	 * @param partitionId - partition number [0 ... partitionCount)
	 * @param partitionCount - number of partition configured for partitioned cache
	 */
	public Collection<Object> getKeysForPartition(int partitionId, int partitionCount);

}
