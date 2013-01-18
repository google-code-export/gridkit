package org.gridkit.coherence.test.rwbm;

import java.util.Collections;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.gridkit.utils.vicluster.CohHelper;
import org.gridkit.utils.vicluster.ViCluster;
import org.gridkit.utils.vicluster.ViNode;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultCacheServer;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.aggregator.Count;
import com.tangosol.util.filter.AlwaysFilter;
import com.tangosol.util.processor.ConditionalPut;

public class LockSemanticCheck {

	static {
		DefaultConfigurableCacheFactory.class.toString();
	}

	@Test(timeout = 30000)
	public void verify_invoke_ignores_lock() {
		
		final String cacheName = "dist-A";
		
		ViCluster cluster = new ViCluster("test_invokation_lock", "org.gridkit", "com.tangosol");
		try {
			
			CohHelper.enableFastLocalCluster(cluster);
			CohHelper.cacheConfig(cluster, "/coherence-cache-config.xml");
			
			ViNode storage = cluster.node("storage");
			CohHelper.localstorage(storage, true);
			
			storage.getCache(cacheName);
			
			ViNode client1 = cluster.node("client1");
			CohHelper.localstorage(client1, false);

			ViNode client2 = cluster.node("client2");
			CohHelper.localstorage(client2, false);
			
			storage.start(DefaultCacheServer.class);
			
			client1.exec(new Callable<Void>(){
				@Override
				public Void call() throws Exception {
					
					NamedCache cache = CacheFactory.getCache(cacheName);

					cache.lock("A");
					
					return null;
				}
			});

			client2.exec(new Callable<Void>(){
				@Override
				public Void call() throws Exception {
					
					NamedCache cache = CacheFactory.getCache(cacheName);
					
					cache.invoke("A", new ConditionalPut(AlwaysFilter.INSTANCE, "A"));
					
					return null;
				}
			});
		}
		finally {
			cluster.shutdown();
		}		
	}
	
	@Test(timeout = 30000)
	public void verify_put_ignores_lock() {
		
		final String cacheName = "dist-A";
		
		ViCluster cluster = new ViCluster("test_invokation_lock", "org.gridkit", "com.tangosol");
		try {
			
			CohHelper.enableFastLocalCluster(cluster);
			CohHelper.cacheConfig(cluster, "/coherence-cache-config.xml");
			
			ViNode storage = cluster.node("storage");
			CohHelper.localstorage(storage, true);
			
			storage.getCache(cacheName);
			
			ViNode client1 = cluster.node("client1");
			CohHelper.localstorage(client1, false);

			ViNode client2 = cluster.node("client2");
			CohHelper.localstorage(client2, false);
			
			storage.start(DefaultCacheServer.class);
			
			client1.exec(new Callable<Void>(){
				@Override
				public Void call() throws Exception {
					
					NamedCache cache = CacheFactory.getCache(cacheName);

					Assert.assertTrue("Lock granted", cache.lock("A"));
					
					return null;
				}
			});

			client2.exec(new Callable<Void>(){
				@Override
				public Void call() throws Exception {
					
					NamedCache cache = CacheFactory.getCache(cacheName);
					
					cache.put("A", "A");
					
					return null;
				}
			});
		}
		finally {
			cluster.shutdown();
		}		
	}		

	@Test(timeout = 30000)
	public void verify_aggregate_ignores_lock() {
		
		final String cacheName = "dist-A";
		
		ViCluster cluster = new ViCluster("test_invokation_lock", "org.gridkit", "com.tangosol");
		try {
			
			CohHelper.enableFastLocalCluster(cluster);
			CohHelper.cacheConfig(cluster, "/coherence-cache-config.xml");
			
			ViNode storage = cluster.node("storage");
			CohHelper.localstorage(storage, true);
			
			storage.getCache(cacheName);
			
			ViNode client1 = cluster.node("client1");
			CohHelper.localstorage(client1, false);
			
			ViNode client2 = cluster.node("client2");
			CohHelper.localstorage(client2, false);
			
			storage.start(DefaultCacheServer.class);
			
			client1.exec(new Callable<Void>(){
				@Override
				public Void call() throws Exception {
					
					NamedCache cache = CacheFactory.getCache(cacheName);
					
					Assert.assertTrue("Lock granted", cache.lock("A"));
					
					return null;
				}
			});
			
			client2.exec(new Callable<Void>(){
				@Override
				public Void call() throws Exception {
					
					NamedCache cache = CacheFactory.getCache(cacheName);
					
					cache.aggregate(Collections.singleton("A"), new Count());
					
					return null;
				}
			});
		}
		finally {
			cluster.shutdown();
		}		
	}		

	@Test(timeout = 30000)
	public void verify_lock_exclusive() {
		
		final String cacheName = "dist-A";
		
		ViCluster cluster = new ViCluster("test_invokation_lock", "org.gridkit", "com.tangosol");
		try {
			
			CohHelper.enableFastLocalCluster(cluster);
			CohHelper.cacheConfig(cluster, "/coherence-cache-config.xml");
			
			ViNode storage = cluster.node("storage");
			CohHelper.localstorage(storage, true);
			
			storage.getCache(cacheName);
			
			ViNode client1 = cluster.node("client1");
			CohHelper.localstorage(client1, false);
			
			ViNode client2 = cluster.node("client2");
			CohHelper.localstorage(client2, false);
			
			storage.start(DefaultCacheServer.class);
			
			client1.exec(new Callable<Void>(){
				@Override
				public Void call() throws Exception {
					
					NamedCache cache = CacheFactory.getCache(cacheName);
					
					Assert.assertTrue("Lock granted", cache.lock("A"));
					
					return null;
				}
			});
			
			client2.exec(new Callable<Void>(){
				@Override
				public Void call() throws Exception {
					
					NamedCache cache = CacheFactory.getCache(cacheName);
					
					Assert.assertFalse("Lock rejected", cache.lock("A"));
					
					return null;
				}
			});
		}
		finally {
			cluster.shutdown();
		}		
	}		
}
