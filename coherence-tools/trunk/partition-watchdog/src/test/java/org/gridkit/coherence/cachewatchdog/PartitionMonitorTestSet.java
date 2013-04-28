package org.gridkit.coherence.cachewatchdog;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import junit.framework.Assert;

import org.gridkit.coherence.cachewatchdog.PartitionMonitor.PartitionListener;
import org.gridkit.coherence.chtest.CohCloud;
import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.coherence.test.CacheTemplate;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.partition.PartitionSet;

public interface PartitionMonitorTestSet {

	public void verify_parallel_recovery_case() throws InterruptedException;

	public void verify_simple_recovery_case() throws InterruptedException;

	public void verify_parallel_init_crash_case() throws InterruptedException;

	public void verify_parallel_init_case_with_limit_and_concurency() throws InterruptedException;

	public void verify_parallel_init_case_with_limit() throws InterruptedException;

	public void verify_parallel_init_case() throws InterruptedException;

	public void verify_vanila_init_case() throws InterruptedException;

	public void verify_fresh_canary_status();

	public static class Impl implements PartitionMonitorTestSet {
	
		public static PartitionMonitorTest_Statics statics() {
			return PartitionMonitorTest_Statics.getInstance();
		}
		
		public CohCloud cloud;
		
		public Impl() {
			PartitionMonitorTest_Statics.reset();
		}
		
		public void setCloud(CohCloud cloud) {
			this.cloud = cloud;
		}
				
		private void assertNoCanaries(CohNode node, final String cacheName) {
			node.exec(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					PartitionMonitor mon = new PartitionMonitor();
					NamedCache cache = CacheFactory.getCache(cacheName);
					PartitionSet ps = mon.getEmptyPartitions(cache);
					Assert.assertEquals("Verify no canaries", true, ps.isFull());
					
					return null;
				}
			});
		}
	
		private void assertAllCanaries(CohNode node, final String cacheName) {
			node.exec(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					PartitionMonitor mon = new PartitionMonitor();
					NamedCache cache = CacheFactory.getCache(cacheName);
					PartitionSet ps = mon.getEmptyPartitions(cache);
					Assert.assertEquals("Verify all canaries", true, ps.isEmpty());
					
					return null;
				}
			});		
		}
		
		private void waitForInit(CohNode node, final String cacheName) {
			node.exec(new Callable<Void>() {
				@SuppressWarnings("unchecked")
				@Override
				public Void call() throws Exception {
					PartitionMonitor mon = new PartitionMonitor();
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
		
		
		private void attachTouchMonitor(CohNode node, final String... cacheNames) {
			attachTouchMonitor(node, Integer.MAX_VALUE, cacheNames);
		}
		
		private void attachTouchMonitor(CohNode node, int limit, final String... cacheNames) {
			attachTouchMonitor(node, limit, Integer.MAX_VALUE, cacheNames);
		}
	
		private void attachTouchMonitor(CohNode node, final int limit, final int concurency, final String... cacheNames) {
			node.exec(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					PartitionMonitor mon = new PartitionMonitor();
					
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
						}, limit, concurency);
					}
					
					return null;
				}
			});
			
		}
		
		@Override
		@Test
		public void verify_fresh_canary_status() {
			
			CacheTemplate.usePartitionedServicePartitionCount(cloud.all(), 100);
	
			cloud.node("server.0").exec(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					PartitionMonitor mon = new PartitionMonitor();
					PartitionSet ps = mon.getEmptyPartitions(CacheFactory.getCache("a-test"));
					Assert.assertEquals("Verify total partition number", 100, ps.getPartitionCount());
					Assert.assertEquals("Verift all are empty", 100, ps.cardinality());
					return null;
				}
			});
		}
	
		@Override
		@Test
		public void verify_vanila_init_case() throws InterruptedException {
			
			final int partitions = 10;
			
			CacheTemplate.usePartitionedServicePartitionCount(cloud.all(), partitions);
			
			cloud.node("server.0").getCache("a-cache1");
			cloud.node("server.0").getCache("a-cache2");
			
			statics().initPartitionCounter(partitions);
			
			cloud.node("client.0").exec(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					PartitionMonitor mon = new PartitionMonitor();
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
			
			assertAllCanaries(cloud.node("client.*"), "a-cache1");
			// second cache should not be affected
			assertNoCanaries(cloud.node("client.*"), "a-cache2");		
		}
	
		@Override
		@Test
		public void verify_parallel_init_case() throws InterruptedException {
			
			final int partitions = 1000;
			
			CacheTemplate.usePartitionedServicePartitionCount(cloud.all(), partitions);
			
			cloud.node("server.0").getCache("a-cache1");
			cloud.node("server.0").getCache("a-cache2");
			
			statics().initPartitionCounter(partitions);
			
			// init Coherence nodes
			cloud.node("client.0");
			cloud.node("client.1");
			cloud.node("client.2");
			cloud.node("client.3");
			
			cloud.node("client.*").getCache("a-cache1");
			
			attachTouchMonitor(cloud.node("client.*"), "a-cache1");
			
			statics().waitAllLatches();
			Thread.sleep(200);
	
			assertAllCanaries(cloud.node("client.*"), "a-cache1");
			// second cache should not be affected
			assertNoCanaries(cloud.node("client.*"), "a-cache2");				
	
			statics().verifyCounters(1);
		}
	
		@Override
		@Test
		public void verify_parallel_init_case_with_limit() throws InterruptedException {
			
			final int partitions = 1000;
			
			CacheTemplate.usePartitionedServicePartitionCount(cloud.all(), partitions);
			
			// init Coherence nodes
			cloud.node("server.0");
			cloud.node("client.0");
			cloud.node("client.1");
			cloud.node("client.2");
			cloud.node("client.3");
			
			statics().initPartitionCounter(partitions);
			
			cloud.all().getCache("a-cache1");
			cloud.all().getCache("a-cache2");
			
			attachTouchMonitor(cloud.node("client.*"), 10, "a-cache1");
			
			statics().waitAllLatches();
			Thread.sleep(200);
			
			assertAllCanaries(cloud.node("client.*"), "a-cache1");
			// second cache should not be affected
			assertNoCanaries(cloud.node("client.*"), "a-cache2");				
			
			statics().verifyCounters(1);
		}
		@Override
		@Test
		public void verify_parallel_init_case_with_limit_and_concurency() throws InterruptedException {
			
			final int partitions = 500;
			
			// init Coherence nodes
			cloud.node("server.0");
			cloud.node("client.0");
			cloud.node("client.1");
			cloud.node("client.2");
			cloud.node("client.3");
	
			CacheTemplate.usePartitionedServicePartitionCount(cloud.all(), partitions);
			
			statics().initPartitionCounter(partitions);
			
			cloud.all().getCache("a-cache1");
			cloud.all().getCache("a-cache2");
			
			attachTouchMonitor(cloud.node("client.*"), 10, 1, "a-cache1");
			
			statics().waitAllLatches();
			Thread.sleep(200);
			
			assertAllCanaries(cloud.node("client.*"), "a-cache1");
			// second cache should not be affected
			assertNoCanaries(cloud.node("client.*"), "a-cache2");				
			
			statics().verifyCounters(1);
		}
	
		@Override
		@Test
		public void verify_parallel_init_crash_case() throws InterruptedException {
			
			final int partitions = 2000;
			final int timeout = 15000;
			
			CacheTemplate.usePartitionedServicePartitionCount(cloud.all(), partitions);
	
			// init Coherence nodes
			cloud.node("server.0");
			cloud.node("client.0");
			cloud.node("client.1");
			cloud.node("client.2");
			cloud.node("client.3");
			cloud.node("client.4");
	
			
			cloud.all().setTCMPTimeout(timeout);
			cloud.all().enableTcpRing(false);
			
			cloud.all().getCache("a-cache1");
			cloud.all().getCache("a-cache2");
			
			statics().initPartitionCounter(partitions);
			
			attachTouchMonitor(cloud.node("client.*"), 20, "a-cache1");
			
			Thread.sleep(500);
			
			System.out.println("Simulating crash for 2,3,4");
			// simulating client crash, verify lock revocation
			cloud.node("client.2").suspend();
			cloud.node("client.3").suspend();
			cloud.node("client.4").suspend();
			
			statics().waitAllLatches();
			System.out.println("Latches are open");
			Thread.sleep(200);
			
			assertAllCanaries(cloud.node("client.*"), "a-cache1");
			// second cache should not be affected
			assertNoCanaries(cloud.node("client.*"), "a-cache2");				
			
			System.out.println("Double init for " + statics().getCounterDiscrepancy(1, 2) + " partitions");
		}
	
		@Override
		@Test
		public void verify_simple_recovery_case() throws InterruptedException {
			
			final int partitions = 100;
			
			CacheTemplate.usePartitionedServicePartitionCount(cloud.all(), partitions);
			CacheTemplate.usePartitionedServiceBackupCount(cloud.all(), 0);
			
			cloud.all().setTCMPTimeout(15000);
			cloud.all().enableTcpRing(false);
			
			// init Coherence nodes
			cloud.node("server.0");
			cloud.node("server.1");
			cloud.node("client.0");
			cloud.node("client.1");
			
			cloud.all().getCache("a-warmup");
			cloud.all().getCache("a-cache1");
			
			statics().initPartitionCounter(partitions);
			
			attachTouchMonitor(cloud.node("client.0"), "a-cache1");
			
			statics().waitAllLatches();
			statics().verifyCounters(1);
			
			assertAllCanaries(cloud.node("client.1"), "a-cache1");
	
			cloud.node("server.0").shutdown();
	
			System.out.println("It takes some time to detect node failure is \"virtualized\" cluster");
			
			waitForInit(cloud.node("client.1"), "a-cache1");
			
			int recovered = statics().getCounterDiscrepancy(1, 2);
			Assert.assertTrue("Atleast few partitions should be recovered: ", recovered > 0);
		}
	
		@Override
		@Test
		public void verify_parallel_recovery_case() throws InterruptedException {
			
			final int partitions = 1000;
			
			CacheTemplate.usePartitionedServicePartitionCount(cloud.all(), partitions);
			CacheTemplate.usePartitionedServiceBackupCount(cloud.all(), 0);
			
			cloud.all().setTCMPTimeout(15000);
			cloud.all().enableTcpRing(false);
			
			// init Coherence nodes
			cloud.node("server.0");
			cloud.node("server.1");
			cloud.node("client.0");
			cloud.node("client.1");
			cloud.node("client.2");
			
			cloud.all().getCache("a-warmup");
			cloud.all().getCache("a-cache1");
			
			statics().initPartitionCounter(partitions);
			
			attachTouchMonitor(cloud.node("client.*"), 10, "a-cache1");
			
			statics().waitAllLatches();
			statics().verifyCounters(1);
			
			assertAllCanaries(cloud.node("client.*"), "a-cache1");
			
			cloud.node("server.0").shutdown();
			System.out.println("It takes some time to detect node failure is \"virtualized\" cluster");
			
			waitForInit(cloud.node("client.*"), "a-cache1");
			
			int recovered = statics().getCounterDiscrepancy(1, 2);
			Assert.assertTrue("Atleast few partitions should be recovered: ", recovered > 0);
		}
	}
}
