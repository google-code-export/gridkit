package org.gridkit.coherence.integration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class SimpleContextTest {

	static ApplicationContext context;
	
	@BeforeClass
	public static void init() {

		System.setProperty("tangosol.coherence.wka", "localhost");
		context = new ClassPathXmlApplicationContext("config/simple-coherence-context.xml");
		
	}
	
	@AfterClass
	public static void shutdown() {
		context = null;
		CacheFactory.getCluster().shutdown();
	}
	
	@Test
	public void testCacheA() {
		NamedCache cache = (NamedCache) context.getBean("cache.A");
		cache.put("a", "b");
		Assert.assertEquals("b", cache.get("a"));
	}
	
	@Test
	public void testCacheB() {
		NamedCache cache = (NamedCache) context.getBean("cache.B");
		Assert.assertEquals("1", cache.get("a"));		
		Assert.assertEquals("2", cache.get("b"));		
	}

	@Test
	public void testCacheC() {
		NamedCache cache = (NamedCache) context.getBean("cache.C");
		cache.put("a", "b");
		Assert.assertEquals("b", cache.get("a"));
	}

	@Test
	public void testCacheD() {
		NamedCache cache = (NamedCache) context.getBean("cache.D");
		cache.put("a", "b");
		Assert.assertEquals("b", cache.get("a"));
		LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(2000));
		// entry should expire
		Assert.assertEquals(null, cache.get("a"));
	}
}
