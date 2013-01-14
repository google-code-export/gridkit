package org.gridkit.coherence.test;

import org.gridkit.utils.vicluster.CohHelper;
import org.gridkit.utils.vicluster.ViCluster;
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

	public static void usePartitionedServiceBackupCount(ViProps props, int threadCount) {
		props.setProp("cache-template.distributed-scheme.backup-count", String.valueOf(threadCount));
	}

	public static void usePartitionedServicePartitionCount(ViProps props, int threadCount) {
		props.setProp("cache-template.distributed-scheme.partition-count", String.valueOf(threadCount));
	}

	public static void usePartitionedServiceGuardianTimeout(ViCluster props, long timeout) {
		usePartitionedServiceGuardianTimeout(props, timeout + "ms");
	}

	public static void usePartitionedServiceGuardianTimeout(ViCluster props, String timeout) {
		props.setProp("cache-template.distributed-scheme.guardian-timeout", timeout);
	}

	public static void usePartitionedServiceTaskTimeout(ViCluster props, long timeout) {
		usePartitionedServiceTaskTimeout(props, timeout + "ms");
	}
	
	public static void usePartitionedServiceTaskTimeout(ViCluster props, String timeout) {
		props.setProp("cache-template.distributed-scheme.task-timeout", timeout);
	}
	
	public static void usePartitionedServiceTaskHungThreshold(ViCluster props, long timeout) {
		usePartitionedServiceTaskHungThreshold(props, timeout + "ms");
	}
	
	public static void usePartitionedServiceTaskHungThreshold(ViCluster props, String timeout) {
		props.setProp("cache-template.distributed-scheme.task-hung-threshold", timeout);
	}
	
	public static void usePartitionedServiceRequestTimeout(ViCluster props, long timeout) {
		usePartitionedServiceRequestTimeout(props, timeout + "ms");
	}
	
	public static void usePartitionedServiceRequestTimeout(ViCluster props, String timeout) {
		props.setProp("cache-template.distributed-scheme.request-timeout", timeout);
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
	
	public static void usePartitionedCacheBackingMapListener(ViProps props, Class<?> bmlistener) {
		props.setProp("cache-template.distributed-scheme.backing-map", "local-cache--with-backing-map-listener");		
		props.setProp("cache-template.backing-map-listener-class", bmlistener.getName());		
	}
}
