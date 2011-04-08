package org.gridkit.coherence.utils.flash;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class FlashTest {
	
	@Test
	public void singlePut() {
		
		// using default coherence-cache-config.xml
		NamedCache cache = CacheFactory.getCache("A");
		cache.clear();
		
		FlashUpdater fu = new FlashUpdater(cache);
		fu.put("A", "A");
		fu.commit();
		
		Assert.assertEquals("A", cache.get("A"));		
		
	}

	
	@Test
	@SuppressWarnings("unchecked")
	public void putAll() {
		
		// using default coherence-cache-config.xml
		NamedCache cache = CacheFactory.getCache("A");
		cache.clear();

		Map<String, String> data = new HashMap<String, String>();
		for(int i = 0; i != 1000; ++i) {
			data.put(String.valueOf(i), String.valueOf(i));
		}
		
		
		FlashUpdater fu = new FlashUpdater(cache);
		fu.putAll(data);
		fu.commit();
		
		Assert.assertEquals(data, new HashMap(cache));		
		
	}

	@Test
	@SuppressWarnings("unchecked")
	public void multiplePutAll() {
		
		// using default coherence-cache-config.xml
		NamedCache cache = CacheFactory.getCache("A");
		cache.clear();
		
		Map<String, String> data = new HashMap<String, String>();
		for(int i = 0; i != 1000; ++i) {
			data.put(String.valueOf(i), String.valueOf(i));
		}
		
		
		FlashUpdater fu = new FlashUpdater(cache);
		fu.putAll(data);
		fu.commit();
		
		Assert.assertEquals(data, new HashMap(cache));
		
		for(Map.Entry<String, String> entry: data.entrySet()) {
			entry.setValue("X");
		}

		fu.putAll(data);
		fu.put("X", "X");
		fu.put("Y", "Y");
		fu.commit();

		
		data.put("X", "X");
		data.put("Y", "Y");
		Assert.assertEquals(data, new HashMap(cache));
		
	}

	@Test
	public void near_singlePut() {
		
		// using default coherence-cache-config.xml
		NamedCache cache = CacheFactory.getCache("near-A");
		cache.clear();
		
		FlashUpdater fu = new FlashUpdater(cache);
		fu.put("A", "A");
		fu.commit();
		
		Assert.assertEquals("A", cache.get("A"));		
		
	}

	
	@Test
	@SuppressWarnings("unchecked")
	public void near_putAll() {
		
		// using default coherence-cache-config.xml
		NamedCache cache = CacheFactory.getCache("near-A");
		cache.clear();

		Map<String, String> data = new HashMap<String, String>();
		for(int i = 0; i != 1000; ++i) {
			data.put(String.valueOf(i), String.valueOf(i));
		}
		
		
		FlashUpdater fu = new FlashUpdater(cache);
		fu.putAll(data);
		fu.commit();
		
		Assert.assertEquals(data, new HashMap(cache));		
		
	}

	@Test
	@SuppressWarnings("unchecked")
	public void near_multiplePutAll() {
		
		// using default coherence-cache-config.xml
		NamedCache cache = CacheFactory.getCache("near-A");
		cache.clear();
		
		Map<String, String> data = new HashMap<String, String>();
		for(int i = 0; i != 1000; ++i) {
			data.put(String.valueOf(i), String.valueOf(i));
		}
		
		
		FlashUpdater fu = new FlashUpdater(cache);
		fu.putAll(data);
		fu.commit();
		
		Assert.assertEquals(data, new HashMap(cache));
		
		for(Map.Entry<String, String> entry: data.entrySet()) {
			entry.setValue("X");
		}

		fu.putAll(data);
		fu.put("X", "X");
		fu.put("Y", "Y");
		fu.commit();

		
		data.put("X", "X");
		data.put("Y", "Y");
		Assert.assertEquals(data, new HashMap(cache));
		
	}
}
