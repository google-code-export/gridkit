package org.gridkit.coherence.check;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.coherence.chtest.CohCloudRule;
import org.gridkit.coherence.chtest.CohHelper;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.gridkit.coherence.test.CacheTemplate;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.coherence.component.util.safeService.SafeCacheService;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Member;
import com.tangosol.net.NamedCache;
import com.tangosol.net.PartitionedService;
import com.tangosol.net.partition.PartitionEvent;
import com.tangosol.net.partition.PartitionListener;
import com.tangosol.net.partition.PartitionSet;

public class PartitionLostEventCheck {

	@Rule
	public CohCloudRule cloud = new DisposableCohCloud();
	private ExecutorService executor = Executors.newCachedThreadPool();

	@After
	public void shutdown_cluster_after_test() {
		executor.shutdownNow();
	}
	
	@Test
	public void verify_two_node_partition_lost_event() throws InterruptedException, ExecutionException {

		cloud.all().presetFastLocalCluster();
		
		CacheTemplate.useTemplateCacheConfig(cloud.all());
		CacheTemplate.usePartitionedInMemoryCache(cloud.all());
		CacheTemplate.usePartitionedServiceBackupCount(cloud.all(), 0);
		CacheTemplate.usePartitionedServicePartitionCount(cloud.all(), 3);
		
		CohNode storage1 = cloud.node("node1");
		CohHelper.localstorage(storage1, true);
		CohNode storage2 = cloud.node("node2");
		CohHelper.localstorage(storage2, true);

		storage1.exec(new LossListener());
		storage2.getCache("a-cache").put("A", "A");
		storage2.getCache("a-cache").put("B", "B");
		storage2.getCache("a-cache").put("C", "C");
		
		storage1.exec(new ShowOwnership());
		
		storage2.shutdown();
		
		Thread.sleep(100);

		storage1.exec(new ShowOwnership());
		
		storage1.exec(new Runnable() {
			@Override
			public void run() {
				Assert.assertTrue(LossListener.LOST.cardinality() > 0);
			}
		});
	}
	
	@SuppressWarnings("serial")
	private final static class LossListener implements Runnable, Serializable {

		public static PartitionSet LOST;
		
		@Override
		public void run() {
			NamedCache cache = CacheFactory.getCache("a-cache");
			PartitionedService service = (PartitionedService)(((SafeCacheService)cache.getCacheService()).getService());
			service.addPartitionListener(new PartitionListener() {
				@Override
				public void onPartitionEvent(PartitionEvent event) {
					if (event.getId() == PartitionEvent.PARTITION_LOST || event.getId() == PartitionEvent.PARTITION_ASSIGNED) {
						System.out.println(event);
					}
					if (event.getId() == PartitionEvent.PARTITION_LOST) {
						if (LOST == null) {
							LOST = new PartitionSet(event.getPartitionSet());
						}
						else {
							LOST.add(event.getPartitionSet());
						}
					}
				}
			});
		}
	}

	@SuppressWarnings("serial")
	private static class ShowOwnership implements Runnable, Serializable {
		
		@Override
		public void run() {
			NamedCache cache = CacheFactory.getCache("a-cache");
			PartitionedService service = (PartitionedService)(cache.getCacheService());
			String[] keys = {"A", "B", "C"};
			for(String key: keys) {
				int p =service.getKeyPartitioningStrategy().getKeyPartition(key);
				Member m = service.getKeyOwner(key);
				System.out.println(key + " [" + p + "] -> " + m); 
			}
		}
	}
}
