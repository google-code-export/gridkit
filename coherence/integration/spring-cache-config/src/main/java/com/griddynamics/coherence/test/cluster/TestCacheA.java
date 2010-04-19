package com.griddynamics.coherence.test.cluster;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class TestCacheA {

	public static void main(String[] args) {
		System.setProperty("tangosol.coherence.wka", "localhost");
		
		CacheFactory.setConfigurableCacheFactory(new TestCacheFactory("com/griddynamics/coherence/test/cluster/test-cache-config-a.xml"));
		NamedCache cache = CacheFactory.getCache("test-a");
		
		cache.put("aaa", "aaa");
		
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

}
