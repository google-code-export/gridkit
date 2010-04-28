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
		applicationContext = new ClassPathXmlApplicationContext("test-context.xml");
	}
	
	@Test
	public void cacheStoreReadWrite() {
		NamedCache cache = applicationContext.getBean("simpleDistributedCache", NamedCache.class);
		assertEquals("12323", cache.get("aaa"));
		assertEquals("asdsad", cache.get("bbb"));
		
		cache.put("aaa", "4232");
		assertEquals("4232", cache.get("aaa"));
	}
	
	@Test
	public void mapListener() {
		NamedCache cache = applicationContext.getBean("simpleDistributedCache", NamedCache.class);
		List<?> events = applicationContext.getBean("cacheEvents", List.class);
		cache.clear();
		events.clear();
		
		cache.put("aaa", "4232");
		cache.put("aaa", "asd1");
		
		assertEquals(2, events.size());
		assertEquals(((MapEvent)events.get(0)).getKey(), "aaa");
		assertEquals(((MapEvent)events.get(0)).getNewValue(), "4232");
		
		assertEquals(((MapEvent)events.get(1)).getKey(), "aaa");
		assertEquals(((MapEvent)events.get(1)).getOldValue(), "4232");
		assertEquals(((MapEvent)events.get(1)).getNewValue(), "asd1");
	}
}
