package org.gridkit.util.coherence.cohtester.examples;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.gridkit.util.coherence.cohtester.CohCloud.CohNode;
import org.gridkit.util.coherence.cohtester.CohCloudRule;
import org.gridkit.util.coherence.cohtester.DisposableCohCloud;
import org.gridkit.zerormi.util.RemoteExporter;
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
	public void test_cqc() throws InterruptedException {
//		cloud.useLocalCluster();
		cloud.all().enableJmx();		
		test_extend_cqc_endurance("cache", 100, 1, 10, 30, 5);
	}
	
	public void test_extend_cqc_endurance(final String cacheName, final int initialCacheSize, final int mutators, final int delta, long execTimeS, long proxyRestartPeriod) throws InterruptedException {
		
		cloud.node("cluster.**")
			.enableFastLocalCluster()
			.cacheConfig("extend-server-cache-config.xml");
		
		cloud.node("xclient.**")
			.disableTCMP()
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
				
		final NamedCache xcqc = cloud.node("xclient").exec(new Callable<NamedCache>() {

			@Override
			public NamedCache call() throws Exception {
				NamedCache cache = CacheFactory.getCache(cacheName);
				NamedCache cqc = new ContinuousQueryCache(cache, AlwaysFilter.INSTANCE);
				return RemoteExporter.export(cqc, NamedCache.class);
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
			
			CohNode prevProxy = cloud.node("cluster.proxy." + lastProxy);
			CohNode nextProxy = cloud.node("cluster.proxy." + ++lastProxy);
			
			if (lastProxy % 2 == 0) {
				// use alternative port
				nextProxy.setProp("test-proxy-port", "33000");
			}
			
			nextProxy.ensureService("ExtendTcpProxyService");
//			prevProxy.shutdown();
//			
//			Thread.sleep(1000);
			
			// verify
			cloud.node("xclient").exec(new Runnable() {
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
//					int size = xcqc.size();
//					if (size < initialCacheSize || size > initialCacheSize + (mutators * delta)) {
//						Assert.assertFalse("Size is " + size, true);
//					}
//					SortedSet<String> keys = new TreeSet<String>();
//					keys.addAll(xcqc.keySet());
//					String max = keys.last();
//					long ts = Long.parseLong(max);
//					long lag = System.nanoTime() - ts;
//					System.out.println("CQC lag: " + TimeUnit.NANOSECONDS.toMillis(lag));
					System.out.println("Hallo");
				}
			});
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
					if (balance < delta && rnd.nextBoolean()) {
						// add key
						String key = "" + System.nanoTime();
						if (cache.invoke(key, new ConditionalPut(notPresent, "", true)) == null) {
							++balance;
							Thread.sleep(10);
						}
					}
					else {
						if (keyCache.isEmpty()) {
							keyCache.addAll(cache.keySet());
						}
						String key = keyCache.remove(rnd.nextInt(keyCache.size()));
						if (cache.remove(key) != null) {
							--balance;
							Thread.sleep(10);
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
