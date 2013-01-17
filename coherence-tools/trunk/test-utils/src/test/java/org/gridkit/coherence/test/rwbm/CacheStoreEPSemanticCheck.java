package org.gridkit.coherence.test.rwbm;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;


import org.gridkit.utils.vicluster.CohHelper;
import org.gridkit.utils.vicluster.ViCluster;
import org.gridkit.utils.vicluster.ViNode;
import org.junit.Test;


import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultCacheServer;
import com.tangosol.net.NamedCache;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

public class CacheStoreEPSemanticCheck {

	@Test
	public void test_simple_store() {
		test_cache_store("simple-A");
	}
	
	public void test_cache_store(final String cacheName) {
		
		ViCluster cluster = new ViCluster("test_cache_loader", "org.gridkit", "com.tangosol");
		try {
			
			CohHelper.enableFastLocalCluster(cluster);
			CohHelper.cacheConfig(cluster, "/cache-store-cache-config.xml");
			
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
					Object old = cache.put("a", "AAA");
					System.out.println("Old a: " + old);
					cache.putAll(Collections.singletonMap("x", "y"));
//					cache.put("b", "BBB");
//					cache.put("c", "CCC");
//					cache.put("d", "DDD");
					
					cache.invokeAll(Arrays.asList("123", "456"), new SimpleEP());
					
					return null;
				}
			});
		}
		finally {
			cluster.shutdown();
		}		
	}
	
	public class SimpleEP extends AbstractProcessor implements Serializable {

		@Override
		public Object process(Entry e) {
			System.out.println("Key:" + e.getKey() + ", present:" + e.isPresent());
//			System.out.println("Key:" + e.getKey() + ", present:" + e.isPresent() + ", value:" + e.getValue());
//			e.setValue("Test");
			e.setValue("Test", true);
			return null;
		}
		
	}
}
