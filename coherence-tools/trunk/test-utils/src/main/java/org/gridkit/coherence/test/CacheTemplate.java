package org.gridkit.coherence.test;

import org.gridkit.utils.vicluster.CohHelper;
import org.gridkit.utils.vicluster.ViProps;

public class CacheTemplate {

	public static void useTemplateCacheConfig(ViProps props) {
		CohHelper.cacheConfig(props, "template-cache-config.xml");
	}
	
	public static void usePartitionedInMemoryCache(ViProps props) {
		props.setProp("cache-template.scheme", "distributed-scheme--local");
	}

	public static void usePartitionedReadWriteCache(ViProps props, Class<?> cacheStoreClass) {
		props.setProp("cache-template.scheme", "distributed-scheme--read-write");
		props.setProp("cache-template.read-write-backing-map.cache-store-class", cacheStoreClass.getName());
	}

	public static void usePartitionedServiceThreadCount(ViProps props, int threadCount) {
		props.setProp("cache-template.distributed-scheme.thread-count", String.valueOf(threadCount));
	}

	public static void useWriteDelay(ViProps props, String delay) {
		props.setProp("cache-template.read-write-backing-map.write-delay", delay);
	}

	public static void useWriteBatchFactor(ViProps props, double f) {
		if (f > 1d || f < 0) {
			throw new IllegalArgumentException("Batch factor " + f + " is out of [0, 1] range");
		}
		props.setProp("cache-template.read-write-backing-map.write-batch-factor", String.valueOf(f));
	}
	
	public static void useMaxWriteBatchSize(ViProps props, int size) {
		props.setProp("cache-template.read-write-backing-map.write-max-batch-size", String.valueOf(size));
	}
}
