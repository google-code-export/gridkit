package com.griddynamics.coherence.integration.spring.classic;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

/**
 * @author Dmitri Babaev
 */
public class ClassicNode {
	
	public static void main(String[] args) {
		System.setProperty("tangosol.coherence.wka", "localhost");
		System.setProperty("tangosol.coherence.cacheconfig", "config/classic/classic-cache-config.xml");

		NamedCache cache = CacheFactory.getCache("test");
		
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
