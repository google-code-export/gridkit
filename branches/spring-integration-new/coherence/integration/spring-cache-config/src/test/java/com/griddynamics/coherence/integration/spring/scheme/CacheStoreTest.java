package com.griddynamics.coherence.integration.spring.scheme;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.tangosol.net.NamedCache;

/**
 * @author Dmitri Babaev
 */
public class CacheStoreTest {
	private ApplicationContext applicationContext;

	@Before
	public void setUp() {
		System.setProperty("tangosol.coherence.wka", "localhost");
		applicationContext = new ClassPathXmlApplicationContext("scheme/test-context.xml");
	}
	
	@Test
	public void cacheStoreReadWrite() {
		NamedCache cache = applicationContext.getBean("simpleDistributedCache", NamedCache.class);
		assertEquals("12323", cache.get("aaa"));
		assertEquals("asdsad", cache.get("bbb"));
		
		cache.put("aaa", "4232");
		assertEquals("4232", cache.get("aaa"));
	}
}
