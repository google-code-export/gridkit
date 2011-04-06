package org.gridkit.coherence.util.dataloss;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;

/**
 * Unit tests
 * @author malekseev
 * 05.04.2011
 */
public class ListenerTest {
	
	private static NamedCache cache;
	
	@BeforeClass
	public static void initCache() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		
		// I can try this technique to programmatically create my canary cache
		//ConfigurablePofContext ctx = new ConfigurablePofContext(XmlHelper.loadXml(getClass().getResource("/test-pof-config.xml")));
		
		CacheFactory.getCluster().shutdown();
		
		System.setProperty("tangosol.coherence.wka", "localhost");
		System.setProperty("tangosol.coherence.localhost", "localhost");
		//        System.setProperty("tangosol.coherence.cacheconfig", "test-pof-cache-config.xml");
		System.setProperty("tangosol.coherence.distributed.localstorage", "true");
		
		CacheFactory.setConfigurableCacheFactory(new DefaultConfigurableCacheFactory("test-canary-cache-config.xml"));
		
		// populate cache with data
		CanaryCachePopulator.populate();
		
		cache = CacheFactory.getCache(CanaryPartitionListener.CACHE_NAME);
		
		/* init cache
		for(int i = 0; i != objects; ++i) {
			cache.put(i, generate(i, SCALE));
		}
		*/
	}
	
	@AfterClass
	public static void shutdown() {
		CacheFactory.getCluster().shutdown();
	}

	@Test
	public void testListener() {
		Assert.assertEquals(true, true);
	}
	
}
