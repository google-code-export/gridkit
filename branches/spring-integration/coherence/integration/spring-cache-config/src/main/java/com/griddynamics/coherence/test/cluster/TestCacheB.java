package com.griddynamics.coherence.test.cluster;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class TestCacheB {

	public static void main(String[] args) {
		System.setProperty("tangosol.coherence.wka", "localhost");

		CacheFactory.setConfigurableCacheFactory(new TestCacheFactory("com/griddynamics/coherence/test/cluster/test-cache-config-b.xml"));
		NamedCache cache = CacheFactory.getCache("test-b");
		
		cache.put("bbb", "bbb");
		
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

}
