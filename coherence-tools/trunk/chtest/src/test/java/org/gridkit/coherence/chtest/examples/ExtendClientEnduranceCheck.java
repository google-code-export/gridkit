package org.gridkit.coherence.chtest.examples;

import java.io.Serializable;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.coherence.chtest.CohCloudRule;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.junit.Assert;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.ContinuousQueryCache;
import com.tangosol.util.Filter;
import com.tangosol.util.filter.AlwaysFilter;
import com.tangosol.util.filter.NotFilter;
import com.tangosol.util.filter.PresentFilter;
import com.tangosol.util.processor.ConditionalPut;

public class ExtendClientEnduranceCheck {

	public CohCloudRule cloud = new DisposableCohCloud();
	
	@Test
	public void test_cqc_in_isolate() throws InterruptedException {
		cloud.all().presetFastLocalCluster();
		// enable Coherence JMX, try to open test runner process in JConsole  
		cloud.all().enableJmx(true);		
		test_extend_cqc_endurance("cache", 100, 1, 10, 300, 5);
	}

	@Test
	public void test_cqc_out_of_process() throws InterruptedException {
		cloud.all().outOfProcess(true);
		cloud.all().enableJmx(true);
		// It is better to have TCP ring for out of process execution
		cloud.all().enableTcpRing(true);
		test_extend_cqc_endurance("cache", 100, 1, 10, 300, 5);
	}
	
	public void test_extend_cqc_endurance(final String cacheName, final int initialCacheSize, final int mutators, final int delta, long execTimeS, long proxyRestartPeriod) throws InterruptedException {
		
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
		
		cloud.node("cluster.storage.1").exec(new Runnable() {
			@Override
			public void run() {
				for(int i = 0; i != initialCacheSize; ++i) {
					NamedCache cache = CacheFactory.getCache(cacheName);
					cache.put("" + (System.nanoTime() + i), "");
				}
			}
		});

		// start extend client
		cloud.node("xclient").getCache(cacheName);
				
		Verifier verifier = cloud.node("xclient").exec(new Callable<Verifier>() {

			@Override
			public Verifier call() throws Exception {
				return new CQCVerifer(cacheName, initialCacheSize, initialCacheSize + mutators * delta);
			}
			
		});
		
		CacheMutator mutator = new CacheMutator(cacheName, delta);
		for(int i = 0; i != mutators; ++i) {
			cloud.node("cluster.storage.1").submit(mutator);
		}

		int lastProxy = 1;
		long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(execTimeS);
		while(deadline > System.currentTimeMillis()) {
			
			Thread.sleep(proxyRestartPeriod);
			
			verifier.verify();
			
			CohNode prevProxy = cloud.node("cluster.proxy." + lastProxy);
			CohNode nextProxy = cloud.node("cluster.proxy." + ++lastProxy);
			
			if (lastProxy % 2 == 0) {
				// use alternative port
				nextProxy.setProp("test-proxy-port", "33000");
			}
			
			nextProxy.getCache(cacheName).size();
			nextProxy.ensureService("ExtendTcpProxyService");
			prevProxy.shutdown();
			
			Thread.sleep(1000);
			verifier.verify();
		}
	}
	
	public interface Verifier extends Remote {
		
		public void verify();
		
	}
	
	public class CQCVerifer implements Verifier {
		
		private final int highBound;
		private final int lowBound;
		private final NamedCache cqc;

		public CQCVerifer(String cacheName, int lowBound,int highBound) {
			this.highBound = highBound;
			this.lowBound = lowBound;
			NamedCache cache = CacheFactory.getCache(cacheName);
			cqc = new ContinuousQueryCache(cache, AlwaysFilter.INSTANCE);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void verify() {
			int size = size();
			if (size < lowBound || size > highBound) {
				Assert.assertFalse("Size is " + size, true);
			}
			SortedSet<String> keys = new TreeSet<String>();
			keys.addAll(cqc.keySet());
			String max = keys.last();
			long ts = Long.parseLong(max);
			long lag = System.nanoTime() - ts;
			System.out.println("CQC lag: " + TimeUnit.NANOSECONDS.toMillis(lag) + " size: " + size);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private int size() {
			return new ArrayList(cqc.keySet()).size();
		}
	}
	
	@SuppressWarnings("serial")
	public static class CacheMutator implements Runnable, Serializable {

		final String cacheName;
		final int delta;		
		
		public CacheMutator(String cacheName, int delta) {
			this.cacheName = cacheName;
			this.delta = delta;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			try {
				NamedCache cache = CacheFactory.getCache(cacheName);
				Random rnd = new Random();
				int balance = 0;
				
				List<String> keyCache = new ArrayList<String>();
				
				Filter present = new PresentFilter();
				Filter notPresent = new NotFilter(present);
	
				while(true) {
					if (balance == 0 || (balance < delta && rnd.nextBoolean())) {
						// add key
						String key = "" + System.nanoTime();
						if (cache.invoke(key, new ConditionalPut(notPresent, "", true)) == null) {
							++balance;
							Thread.sleep(30);
						}
					}
					else {
						if (keyCache.isEmpty()) {
							keyCache.addAll(cache.keySet());
						}
						String key = keyCache.remove(rnd.nextInt(keyCache.size()));
						if (cache.remove(key) != null) {
							--balance;
							Thread.sleep(30);
						}
					}
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
