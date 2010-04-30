package com.griddynamics.coherence.integration.spring;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.tangosol.net.NamedCache;
import com.tangosol.util.MapEvent;

/**
 * @author Dmitri Babaev
 */
public class ContextConfigurationTest {
	private ApplicationContext applicationContext;

	@Before
	public void setUp() {
		System.setProperty("tangosol.coherence.wka", "localhost");
		applicationContext = new ClassPathXmlApplicationContext("config/test-context.xml");
	}
	
	@Test
	public void cacheStoreReadWrite() {
		NamedCache cache = applicationContext.getBean("distributedStoreCache", NamedCache.class);
		assertEquals("12323", cache.get("aaa"));
		assertEquals("asdsad", cache.get("bbb"));
		
		cache.put("aaa", "4232");
		assertEquals("4232", cache.get("aaa"));
	}
	
	@Test
	public void nearCacheTest() {
		NamedCache cache = applicationContext.getBean("distributedNearCache", NamedCache.class);
	
		cache.put("aaa", "4232");
		assertEquals("4232", cache.get("aaa"));
	}
	
	@Test
	public void mapListener() {
		NamedCache cache = applicationContext.getBean("dstributedHistoryCache", NamedCache.class);
		List<?> events = applicationContext.getBean("cacheEvents", List.class);
		cache.clear();
		events.clear();
		
		cache.put("aaa", "4232");
		cache.put("aaa", "asd1");
		
		assertEquals(1, cache.size());
		
		for (int i = 0; i < 3; ++i) {
			if (events.size() < 2)
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					break;
				}
		}
		
		assertEquals(2, events.size());
		assertEquals(((MapEvent)events.get(0)).getKey(), "aaa");
		assertEquals(((MapEvent)events.get(0)).getNewValue(), "4232");
		
		assertEquals(((MapEvent)events.get(1)).getKey(), "aaa");
		assertEquals(((MapEvent)events.get(1)).getOldValue(), "4232");
		assertEquals(((MapEvent)events.get(1)).getNewValue(), "asd1");
	}
}
