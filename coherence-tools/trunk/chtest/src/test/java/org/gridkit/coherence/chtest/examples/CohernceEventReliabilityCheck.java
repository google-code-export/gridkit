package org.gridkit.coherence.chtest.examples;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.coherence.chtest.CohCloudRule;
import org.gridkit.coherence.chtest.CohHelper;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.gridkit.util.concurrent.Barriers;
import org.gridkit.util.concurrent.BlockingBarrier;
import org.gridkit.vicluster.telecontrol.jvm.JvmProps;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.ContinuousQueryCache;
import com.tangosol.util.CompositeKey;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;
import com.tangosol.util.filter.AlwaysFilter;

public class CohernceEventReliabilityCheck {
	
	@Rule
	public CohCloudRule cloud = new DisposableCohCloud();	

	@Test
	public void test_extend_cqc_event_reliability_out_of_proc() throws InterruptedException {
		cloud.all().presetFastLocalCluster();
		cloud.all().outOfProcess(true);
		JvmProps.at(cloud.all()).addJvmArg("-Xmx300m");
		JvmProps.at(cloud.all()).addJvmArg("-Xms300m");
		
		cloud.all().enableJmx(true);
		// It is better to have TCP ring for out of process execution
		cloud.all().enableTcpRing(true);
		
		test_extend_cqc_event_reliability("test", 1000, 0.5, TimeUnit.MINUTES.toMillis(5), 5000);
	}

	@Test
	public void test_cluster_event_reliability_out_of_proc() throws InterruptedException {
		cloud.all().presetFastLocalCluster();
		cloud.all().outOfProcess(true);
		cloud.all().logLevel(1);
		JvmProps.at(cloud.all()).addJvmArg("-Xmx300m");
		JvmProps.at(cloud.all()).addJvmArg("-Xms300m");
		
		cloud.all().enableJmx(true);
		// It is better to have TCP ring for out of process execution
		cloud.all().enableTcpRing(true);
		
		test_cluster_event_reliability("test", 1000, 200, TimeUnit.MINUTES.toMillis(5), 5000);
	}
	
	public void test_extend_cqc_event_reliability(final String cacheName, final int cacheSize, double updateRate, long execTimeS, long proxyRestartPeriod) throws InterruptedException {
		
		cloud.node("cluster.**")
			.cacheConfig("extend-server-cache-config.xml");
				
		cloud.node("xclient.**")
			.enableTCMP(false)
			.cacheConfig("extend-client-cache-config.xml");
		
		cloud.node("cluster.storage.**")
			.autoStartServices()
			.localStorage(true);

		cloud.node("cluster.proxy.**")
			.autoStartServices()
			.localStorage(false);
		
		cloud.node("cluster.storage.1");
		cloud.node("cluster.proxy.1");
		
		// start cluster and init named cache 
		cloud.node("cluster.**").getCache(cacheName);
		cloud.node("cluster.proxy.1").ensureService("ExtendTcpProxyService");
		
		// start extend client
		cloud.node("xclient").getCache(cacheName);
				
		cloud.node("xclient").exec(new CQCEventObserver(cacheName, "A"));
		System.out.println("Observer started");
		cloud.node("cluster.storage.1").submit(new CacheMutator(cacheName, "A", cacheSize, updateRate));
		System.out.println("Mutator started");
		
		int lastProxy = 1;
		long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(execTimeS);
		while(deadline > System.currentTimeMillis()) {
			
			Thread.sleep(proxyRestartPeriod);
			
			CohNode prevProxy = cloud.node("cluster.proxy." + lastProxy);
			CohNode nextProxy = cloud.node("cluster.proxy." + ++lastProxy);
			
			if (lastProxy % 2 == 0) {
				// use alternative port
				nextProxy.setProp("test-proxy-port", "33000");
			}
			
			nextProxy.getCache(cacheName).size();
			nextProxy.ensureService("ExtendTcpProxyService");
			System.out.println("Killing proxy");
			prevProxy.kill();
		}
	}

	public void test_cluster_event_reliability(final String cacheName, final int cacheSize, double updateRate, long execTimeS, long proxyRestartPeriod) throws InterruptedException {
		
		cloud.node("cluster.**")
			.cacheConfig("extend-server-cache-config.xml");
				
		cloud.node("cluster.storage.**")
			.autoStartServices()
			.localStorage(true);

		cloud.node("cluster.proxy.**")
			.autoStartServices()
			.localStorage(false);
		
		cloud.nodes("cluster.storage.0", "cluster.storage.1", "cluster.storage.2");
		cloud.nodes("cluster.proxy.1", "cluster.proxy.2");
		
		// proxy should be senior
		cloud.node("cluster.proxy.**").ensureCluster();
		cloud.node("cluster.storage.**").ensureCluster();
		
		// start cluster and init named cache 
		cloud.node("cluster.**").getCache(cacheName);
						
		cloud.node("cluster.proxy.1").exec(new EventObserver(cacheName, "A"));
		System.out.println("Observer started");
		cloud.node("cluster.proxy.2").submit(new CacheMutator(cacheName, "A", cacheSize, updateRate));
		// extra mutator should keep service busy
		cloud.node("cluster.proxy.2").submit(new CacheBulkMutator(cacheName, "B", cacheSize, Math.min(1000, cacheSize), updateRate));
		System.out.println("Mutators started");
		
		int lastNode = 2;
		long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(execTimeS);
		while(deadline > System.currentTimeMillis()) {
			
			Thread.sleep(proxyRestartPeriod);
			
			CohNode prevNode = cloud.node("cluster.storage." + (lastNode - 2));
			CohNode nextNode = cloud.node("cluster.storage." + ++lastNode);
			
			nextNode.getCache(cacheName).size();
			System.out.println("Killing storage");
			prevNode.kill();
			
			CohHelper.jmxWaitForStatusHA(nextNode, "SimpleCacheService", "NODE-SAFE");
		}
	}
	
	@SuppressWarnings("serial")
	public static class CQCEventObserver extends EventObserver {

		public CQCEventObserver(String cacheName, Object primaryKey) {
			super(cacheName, primaryKey);
			// TODO Auto-generated constructor stub
		}

		@Override
		NamedCache getCache() {
			final ContinuousQueryCache cache = new ContinuousQueryCache(CacheFactory.getCache(cacheName), AlwaysFilter.INSTANCE);
			// CQC will not reconnect automatically
			// we need to kick it periodically
			new Thread("CQC-pinger") {
				@Override
				public void run() {
					while(true) {
						try {
							Thread.sleep(1000);
							System.out.println("CQC size is " + cache.size());
						} catch (Exception e) {
							// ignore
						}
					}
				}
			}.start();
			return cache;
		}
	}
	
	@SuppressWarnings("serial")
	public static class EventObserver implements Runnable, Serializable {
		
		final String cacheName;
		final Object primaryKey;
		
		
		long nextExpected = Long.MIN_VALUE;

		public EventObserver(String cacheName, Object primaryKey) {
			this.cacheName = cacheName;
			this.primaryKey = primaryKey;
		}
		
		NamedCache getCache() {
			return CacheFactory.getCache(cacheName);
		}
		
		@Override
		public void run() {
			try {
				final NamedCache cache = getCache();
				cache.addMapListener(new MapListener() {
					
					@Override
					public void entryUpdated(MapEvent a) {
						process(a);
					}
					
					@Override
					public void entryInserted(MapEvent b) {
						process(b);
					}

					@Override
					public void entryDeleted(MapEvent arg0) {
						// do nothing						
					}

				}, AlwaysFilter.INSTANCE, false);				
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		private void process(MapEvent b) {
			if (((CompositeKey)b.getKey()).getPrimaryKey().equals(primaryKey)) {
//				System.out.println(b);
				if (nextExpected == Long.MIN_VALUE) {
					nextExpected = ((Long)b.getNewValue()) + 1; 
				}
				else {
					long sn = ((Long)b.getNewValue());
					if (sn > nextExpected) {
						System.out.println("Event gap: " + nextExpected + "-" + (sn - 1));
						nextExpected = sn + 1;
					}
					else if (sn < nextExpected) {
						System.out.println("Out of bound event: " + sn + " (last seen " + (nextExpected - 1) + ")");
					}
					else {
						nextExpected = sn + 1;
					}
				}
			}
		}							
	}
	
	@SuppressWarnings("serial")
	public static class CacheMutator implements Runnable, Serializable {

		final String cacheName;
		final Object primaryKey;
		final int targetSize;
		final double rate;
		
		public CacheMutator(String cacheName, Object primaryKey, int targetSize, double rate) {
			this.cacheName = cacheName;
			this.primaryKey = primaryKey;
			this.targetSize = targetSize;
			this.rate = rate;
		}

		@Override
		public void run() {
			try {
				BlockingBarrier barrier = Barriers.speedLimit(rate);
				NamedCache cache = CacheFactory.getCache(cacheName);
				Random rnd = new Random();
				long counter = 0;
				
				while(true) {
					barrier.pass();
					int n = rnd.nextInt(targetSize);
					CompositeKey nkey = new CompositeKey(primaryKey, n);
					cache.put(nkey, ++counter);
//					System.out.println("Update: " + nkey + " <- " + counter);
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("serial")
	public static class CacheBulkMutator implements Runnable, Serializable {
		
		final String cacheName;
		final Object primaryKey;
		final int targetSize;
		final int batchSize;
		final double rate;
		
		public CacheBulkMutator(String cacheName, Object primaryKey, int targetSize, int batchSize, double rate) {
			this.cacheName = cacheName;
			this.primaryKey = primaryKey;
			this.targetSize = targetSize;
			this.batchSize = batchSize;
			this.rate = rate;
		}
		
		@Override
		public void run() {
			try {
				BlockingBarrier barrier = Barriers.speedLimit(rate);
				NamedCache cache = CacheFactory.getCache(cacheName);
				Random rnd = new Random();
				long counter = 0;
				
				Map<Object, Object> batch = new HashMap<Object, Object>();
				
				while(true) {
					int n = rnd.nextInt(targetSize);
					CompositeKey nkey = new CompositeKey(primaryKey, n);
					batch.put(nkey, ++counter);
					if (batch.size() >= batchSize) {
						barrier.pass();
						cache.putAll(batch);
						batch.clear();
					}
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
