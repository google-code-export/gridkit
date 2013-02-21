package org.gridkit.util.coherence.cohtester;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.gridkit.util.coherence.cohtester.CohCloud.CohNode;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.AbstractCacheStore;

public class CacheStoreCheck {

	@Test
	public void test_cache_store() {
		test_cache_store("vanila-A");
	}

	@Rule
	public CohCloudRule cloud = new DisposableCohCloud();
	
	public void test_cache_store(final String cacheName) {
		
		cloud.node("**")
			.enableFastLocalCluster()
			.cacheConfig("/cache-store-cache-config.xml");
		
		cloud.node("**").setProp("test-cache-store-class", TestStore.class.getName());
			

		CohNode storage = cloud.node("storage");
		storage.localStorage(true);
		storage.autoStartServices();
		
		CohNode client = cloud.node("client");
		client.localStorage(false);
		
		cloud.node("**").getCache(cacheName);
		
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
