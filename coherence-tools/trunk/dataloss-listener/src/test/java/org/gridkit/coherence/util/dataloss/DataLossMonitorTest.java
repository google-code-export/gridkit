package org.gridkit.coherence.util.dataloss;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import junit.framework.Assert;

import org.gridkit.coherence.test.CacheTemplate;
import org.gridkit.coherence.util.dataloss.DataLossMonitor.PartitionListener;
import org.gridkit.utils.vicluster.CohHelper;
import org.gridkit.utils.vicluster.ViCluster;
import org.gridkit.utils.vicluster.ViNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.partition.PartitionSet;

public class DataLossMonitorTest {

	public static DataLossMonitorTest_Statics statics() {
		return DataLossMonitorTest_Statics.getInstance();
	}
	
	private ViCluster cluster = new ViCluster("data-loss-test", "com.tangosol", "org.gridkit");
	{
		cluster.shareClass(DataLossMonitorTest_Statics.class);
		CohHelper.enableFastLocalCluster(cluster);
		CacheTemplate.useTemplateCacheConfig(cluster);
		CacheTemplate.usePartitionedInMemoryCache(cluster);
	}
	
	@Before
	public void initCluster() {	
		System.gc();
		cluster = new ViCluster("data-loss-test", "com.tangosol", "org.gridkit");
		cluster.shareClass(DataLossMonitorTest_Statics.class);
		CohHelper.enableFastLocalCluster(cluster);
		CacheTemplate.useTemplateCacheConfig(cluster);
		CacheTemplate.usePartitionedInMemoryCache(cluster);
	}
	
	@After
	public void afterTest() {
		System.gc();
		System.err.println("Killing cluster");
		cluster.silence();
		cluster.shutdown();
		cluster = null;
	}

	@After
	public void resetStatics() {
		DataLossMonitorTest_Statics.reset();
	}
	
	private ViNode server(int n) {
		ViNode node = cluster.node("server-" + n);
		CohHelper.localstorage(node, true);
		return node;
	}

	private ViNode client(int n) {
		ViNode node = cluster.node("client-" + n);
		CohHelper.localstorage(node, false);
		return node;		
	}
	
	private void assertNoCanaries(int n, final String cacheName) {
		client(n).exec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				DataLossMonitor mon = new DataLossMonitor();
				NamedCache cache = CacheFactory.getCache(cacheName);
				PartitionSet ps = mon.getEmptyPartitions(cache);
				Assert.assertEquals("Verify no canaries", true, ps.isFull());
				
				return null;
			}
		});
	}

	private void assertAllCanaries(int n, final String cacheName) {
		client(n).exec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				DataLossMonitor mon = new DataLossMonitor();
				NamedCache cache = CacheFactory.getCache(cacheName);
				PartitionSet ps = mon.getEmptyPartitions(cache);
				Assert.assertEquals("Verify all canaries", true, ps.isEmpty());
				
				return null;
			}
		});		
	}
	
	private void waitForInit(int n, final String cacheName) {
		client(n).exec(new Callable<Void>() {
			@SuppressWarnings("unchecked")
			@Override
			public Void call() throws Exception {
				DataLossMonitor mon = new DataLossMonitor();
				int n = 0;
				while(true) {
					NamedCache cache = CacheFactory.getCache(cacheName);
					PartitionSet ps = mon.getEmptyPartitions(cache);
					if (ps.isEmpty()) {
						return null;
					}
					else {
						Thread.sleep(100);
						++n;
						if (n % 300 == 0) {
							Map<Object, Object> canaries = new HashMap<Object, Object>(CacheFactory.getCache(cacheName).getCacheService().ensureCache("CANARY_CACHE", null));
							for(Object key: canaries.keySet()) {
								System.out.println("  " + key + " -> " + canaries.get(key));
							}
						}
					}
				}
			}
		});
	}
	
	
	private void attachTouchMonitor(int n, final String... cacheNames) {
		attachTouchMonitor(n, Integer.MAX_VALUE, cacheNames);
	}

	private void attachTouchMonitor(int n, final int limit, final String... cacheNames) {
		client(n).exec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				DataLossMonitor mon = new DataLossMonitor();
				
				for(String cacheName: cacheNames) {
					System.out.println("Attaching monitor for '" + cacheName + "'");
					mon.attachPartitionMonitor(CacheFactory.getCache(cacheName), new PartitionListener() {
						@Override
						public void onEmptyPartition(NamedCache cache, PartitionSet partitions) {
							System.out.println("Processing: "  + partitions);
							for(int p: partitions.toArray()) {
								statics().touchPartition(p);
							}
						}
					}, limit);
				}
				
				return null;
			}
		});
		
	}
	
	@Test
	public void verify_fresh_canary_status() {
		
		CacheTemplate.usePartitionedServicePartitionCount(cluster, 100);

		server(0).exec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				DataLossMonitor mon = new DataLossMonitor();
				PartitionSet ps = mon.getEmptyPartitions(CacheFactory.getCache("a-test"));
				Assert.assertEquals("Verify total partition number", 100, ps.getPartitionCount());
				Assert.assertEquals("Verift all are empty", 100, ps.cardinality());
				return null;
			}
		});
	}

	@Test
	public void verify_vanila_init_case() throws InterruptedException {
		
		final int partitions = 10;
		
		CacheTemplate.usePartitionedServicePartitionCount(cluster, partitions);
		
		server(0).getCache("a-cache1");
		server(0).getCache("a-cache2");
		
		statics().initPartitionCounter(partitions);
		
		client(0).exec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				DataLossMonitor mon = new DataLossMonitor();
				NamedCache cache = CacheFactory.getCache("a-cache1");
				PartitionSet ps = mon.getEmptyPartitions(cache);
				Assert.assertEquals("Verify total partition number", partitions, ps.getPartitionCount());
				Assert.assertEquals("Verift all are empty", partitions, ps.cardinality());
				
				System.out.println("Attaching monitor");
				mon.attachPartitionMonitor(cache, new PartitionListener() {
					@Override
					public void onEmptyPartition(NamedCache cache, PartitionSet partitions) {
						System.out.println("Processing: " + partitions);
						for(int p: partitions.toArray()) {
							statics().touchPartition(p);
						}
						LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(partitions.cardinality()));
					}
				});
				
				return null;
			}
		});
		
		statics().waitAllLatches();
		statics().verifyCounters(1);
		
		assertAllCanaries(1, "a-cache1");
		// second cache should not be affected
		assertNoCanaries(1, "a-cache2");		
	}

	@Test
	public void verify_parallel_init_case() throws InterruptedException {
		
		final int partitions = 1000;
		
		CacheTemplate.usePartitionedServicePartitionCount(cluster, partitions);
		
		server(0).getCache("a-cache1");
		server(0).getCache("a-cache2");
		
		statics().initPartitionCounter(partitions);
		
		// init Coherence nodes
		client(0).getCache("a-cache1");
		client(1).getCache("a-cache1");
		client(2).getCache("a-cache1");
		client(3).getCache("a-cache1");
		
		attachTouchMonitor(0, "a-cache1");
		attachTouchMonitor(1, "a-cache1");
		attachTouchMonitor(2, "a-cache1");
		attachTouchMonitor(3, "a-cache1");
		
		statics().waitAllLatches();
		Thread.sleep(200);

		assertAllCanaries(1, "a-cache1");
		// second cache should not be affected
		assertNoCanaries(1, "a-cache2");				

		statics().verifyCounters(1);
	}

	@Test
	public void verify_parallel_init_case_with_limit() throws InterruptedException {
		
		final int partitions = 1000;
		
		CacheTemplate.usePartitionedServicePartitionCount(cluster, partitions);
		
		server(0).getCache("a-cache1");
		server(0).getCache("a-cache2");
		
		statics().initPartitionCounter(partitions);
		
		// init Coherence nodes
		client(0).getCache("a-cache1");
		client(1).getCache("a-cache1");
		client(2).getCache("a-cache1");
		client(3).getCache("a-cache1");
		
		attachTouchMonitor(0, 10, "a-cache1");
		attachTouchMonitor(1, 10, "a-cache1");
		attachTouchMonitor(2, 10, "a-cache1");
		attachTouchMonitor(3, 10, "a-cache1");
		
		statics().waitAllLatches();
		Thread.sleep(200);
		
		assertAllCanaries(1, "a-cache1");
		// second cache should not be affected
		assertNoCanaries(1, "a-cache2");				
		
		statics().verifyCounters(1);
	}

	@Test
	public void verify_parallel_init_crash_case() throws InterruptedException {
		
		final int partitions = 2000;
		final int timeout = 15000;
		
		CacheTemplate.usePartitionedServicePartitionCount(cluster, partitions);
		
//		CohHelper.enableJmx(server(0));
//		CohHelper.enableJmx(client(0));
		CohHelper.setTCMPTimeout(server(0), timeout);
		CohHelper.disableTcpRing(server(0));
		CohHelper.setTCMPTimeout(client(0), timeout);
		CohHelper.disableTcpRing(client(0));
		CohHelper.setTCMPTimeout(client(1), timeout);
		CohHelper.disableTcpRing(client(1));
		CohHelper.setTCMPTimeout(client(2), timeout);
		CohHelper.disableTcpRing(client(2));
		CohHelper.setTCMPTimeout(client(3), timeout);
		CohHelper.disableTcpRing(client(3));
		CohHelper.setTCMPTimeout(client(4), timeout);
		CohHelper.disableTcpRing(client(4));
		CohHelper.setTCMPTimeout(client(5), timeout);
		CohHelper.disableTcpRing(client(5));
		
		server(0).getCache("a-cache1");
		server(0).getCache("a-cache2");
		
		statics().initPartitionCounter(partitions);
		
		ManagementFactory.getPlatformMBeanServer().queryNames(null, null);
		
		// init Coherence nodes
		client(0).getCache("a-cache1");
		client(1).getCache("a-cache1");
		client(2).getCache("a-cache1");
		client(3).getCache("a-cache1");
		client(4).getCache("a-cache1");
		client(5).getCache("a-cache1");
		
		attachTouchMonitor(0, 20, "a-cache1");
		attachTouchMonitor(1, 20, "a-cache1");
		attachTouchMonitor(2, 20, "a-cache1");
		attachTouchMonitor(3, 20, "a-cache1");
		attachTouchMonitor(4, 20, "a-cache1");
		attachTouchMonitor(5, 20, "a-cache1");
		
		Thread.sleep(500);
		
		System.out.println("Simulating crash for 2,3,4,5");
		// simulating client crash, verify lock revocation
		client(2).suspend();
		client(3).suspend();
		client(4).suspend();
		client(5).suspend();
		
		statics().waitAllLatches();
		System.out.println("Latches are open");
		Thread.sleep(200);
		
		assertAllCanaries(0, "a-cache1");
		// second cache should not be affected
		assertNoCanaries(0, "a-cache2");				
		
		System.out.println("Double init for " + statics().getCounterDiscrepancy(1, 2) + " partitions");
		// shutdown may not work well here
		cluster.silence();
		cluster.kill();
	}

	@Test
	public void verify_simple_recovery_case() throws InterruptedException {
		
		final int partitions = 100;
		
		CacheTemplate.usePartitionedServicePartitionCount(cluster, partitions);
		CacheTemplate.usePartitionedServiceBackupCount(cluster, 0);
		
		server(0).getCache("a-warmup");
		server(1).getCache("a-warmup");

		server(0).getCache("a-cache1");
		server(1).getCache("a-cache1");
		
		statics().initPartitionCounter(partitions);
		
		attachTouchMonitor(0, "a-cache1");
		
		statics().waitAllLatches();
		statics().verifyCounters(1);
		
		assertAllCanaries(1, "a-cache1");
		
		server(0).kill();
		
		waitForInit(1, "a-cache1");
		
		int recovered = statics().getCounterDiscrepancy(1, 2);
		Assert.assertTrue("Atleast few partitions should be recovered: ", recovered > 0);
		
	}

	@Test
	public void verify_parallel_recovery_case() throws InterruptedException {
		
		final int partitions = 1000;
		
		CacheTemplate.usePartitionedServicePartitionCount(cluster, partitions);
		CacheTemplate.usePartitionedServiceBackupCount(cluster, 0);
		
		server(0).getCache("a-warmup");
		server(1).getCache("a-warmup");
		server(2).getCache("a-warmup");
		
		server(0).getCache("a-cache1");
		server(1).getCache("a-cache1");
		server(2).getCache("a-cache1");
		
		statics().initPartitionCounter(partitions);
		
		attachTouchMonitor(0, 10, "a-cache1");
		attachTouchMonitor(1, 10, "a-cache1");
		attachTouchMonitor(2, 10, "a-cache1");
		attachTouchMonitor(3, 10, "a-cache1");
		
		statics().waitAllLatches();
		statics().verifyCounters(1);
		
		assertAllCanaries(4, "a-cache1");
		
		server(0).kill();
		
		waitForInit(4, "a-cache1");
		
		int recovered = statics().getCounterDiscrepancy(1, 2);
		Assert.assertTrue("Atleast few partitions should be recovered: ", recovered > 0);
	}
}
