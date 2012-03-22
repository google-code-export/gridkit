package org.gridkit.coherence.util.vicluster;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.gridkit.utils.vicluster.CohHelper;
import org.gridkit.utils.vicluster.ViCluster;
import org.gridkit.utils.vicluster.ViNode;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultCacheServer;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.AbstractCacheStore;

public class CacheStoreCheck {

	@Test
	public void test_cache_store() {
		test_cache_store("vanila-A");
	}

	public void test_cache_store(final String cacheName) {
		
		ViCluster cluster = new ViCluster("test_cache_store", "org.gridkit", "com.tangosol");
		try {
			
			CohHelper.enableFastLocalCluster(cluster);
			CohHelper.cacheConfig(cluster, "cache-store-cache-config.xml");
			
			ViNode storage = cluster.node("storage");
			CohHelper.localstorage(storage, true);
			
			storage.getCache(cacheName);
			
			ViNode client = cluster.node("client");
			CohHelper.localstorage(client, false);
			
			storage.start(DefaultCacheServer.class);
			
			client.exec(new Callable<Void>(){
				@Override
				public Void call() throws Exception {
					
					NamedCache cache = CacheFactory.getCache(cacheName);
					
					Map<Integer, String> values = new HashMap<Integer, String>();
					values.put(0, "0");
					values.put(1, "1");
					values.put(2, "2");
					values.put(3, "3");
					values.put(4, "4");
					values.put(5, "5");
					values.put(6, "6");

					cache.putAll(values);					
					
					return null;
				}
			});
		}
		finally {
			cluster.shutdown();
		}		
	}
	
	
	public static class TestStore extends AbstractCacheStore {

		@Override
		public void store(Object oKey, Object oValue) {
			System.out.println("store: " + oKey + " -> " + oValue);
		}

		@Override
		@SuppressWarnings("rawtypes")
		public void storeAll(Map value) {
			System.out.println("storeAll: " + value );
		}

		@Override
		public Object load(Object key) {
			return null;
		}
	}
}
