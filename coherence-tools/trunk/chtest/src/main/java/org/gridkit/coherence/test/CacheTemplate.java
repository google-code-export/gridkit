package org.gridkit.coherence.test;

import org.gridkit.util.coherence.cohtester.CohHelper;
import org.gridkit.vicluster.ViConfigurable;

public class CacheTemplate {

	public static void useTemplateCacheConfig(ViConfigurable props) {
		CohHelper.cacheConfig(props, "template-cache-config.xml");
	}
	
	public static void usePartitionedInMemoryCache(ViConfigurable props) {		
		props.setProp("cache-template.scheme", "distributed-scheme--local");
	}

	public static void usePartitionedReadWriteCache(ViConfigurable props, Class<?> cacheStoreClass) {
		props.setProp("cache-template.scheme", "distributed-scheme--read-write");
		props.setProp("cache-template.read-write-backing-map.cache-store-class", cacheStoreClass.getName());
	}

	public static void usePartitionedServiceThreadCount(ViConfigurable props, int threadCount) {
		props.setProp("cache-template.distributed-scheme.thread-count", String.valueOf(threadCount));
	}

	public static void usePartitionedServiceBackupCount(ViConfigurable props, int threadCount) {
		props.setProp("cache-template.distributed-scheme.backup-count", String.valueOf(threadCount));
	}

	public static void usePartitionedServicePartitionCount(ViConfigurable props, int threadCount) {
		props.setProp("cache-template.distributed-scheme.partition-count", String.valueOf(threadCount));
	}

	public static void usePartitionedServiceGuardianTimeout(ViConfigurable props, long timeout) {
		usePartitionedServiceGuardianTimeout(props, timeout + "ms");
	}

	public static void usePartitionedServiceGuardianTimeout(ViConfigurable props, String timeout) {
		props.setProp("cache-template.distributed-scheme.guardian-timeout", timeout);
	}

	public static void usePartitionedServiceTaskTimeout(ViConfigurable props, long timeout) {
		usePartitionedServiceTaskTimeout(props, timeout + "ms");
	}
	
	public static void usePartitionedServiceTaskTimeout(ViConfigurable props, String timeout) {
		props.setProp("cache-template.distributed-scheme.task-timeout", timeout);
	}
	
	public static void usePartitionedServiceTaskHungThreshold(ViConfigurable props, long timeout) {
		usePartitionedServiceTaskHungThreshold(props, timeout + "ms");
	}
	
	public static void usePartitionedServiceTaskHungThreshold(ViConfigurable props, String timeout) {
		props.setProp("cache-template.distributed-scheme.task-hung-threshold", timeout);
	}
	
	public static void usePartitionedServiceRequestTimeout(ViConfigurable props, long timeout) {
		usePartitionedServiceRequestTimeout(props, timeout + "ms");
	}
	
	public static void usePartitionedServiceRequestTimeout(ViConfigurable props, String timeout) {
		props.setProp("cache-template.distributed-scheme.request-timeout", timeout);
	}
	
	public static void useWriteDelay(ViConfigurable props, String delay) {
		props.setProp("cache-template.read-write-backing-map.write-delay", delay);
	}

	public static void useWriteBatchFactor(ViConfigurable props, double f) {
		if (f > 1d || f < 0) {
			throw new IllegalArgumentException("Batch factor " + f + " is out of [0, 1] range");
		}
		props.setProp("cache-template.read-write-backing-map.write-batch-factor", String.valueOf(f));
	}
	
	public static void useMaxWriteBatchSize(ViConfigurable props, int size) {
		props.setProp("cache-template.read-write-backing-map.write-max-batch-size", String.valueOf(size));
	}
	
	public static void usePartitionedCacheBackingMapListener(ViConfigurable props, Class<?> bmlistener) {
		props.setProp("cache-template.distributed-scheme.backing-map", "local-cache--with-backing-map-listener");		
		props.setProp("cache-template.backing-map-listener-class", bmlistener.getName());		
	}
}
