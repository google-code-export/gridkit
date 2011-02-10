package org.apteka.insurance.demo.cache.memory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apteka.insurance.demo.cache.CacheService;
import org.springframework.util.Assert;

public class CacheServiceImpl implements CacheService {
	private ConcurrentMap<String, ConcurrentMap<Object, Object>> caches = new ConcurrentHashMap<String, ConcurrentMap<Object,Object>>();
	
	public ConcurrentMap<Object, Object> getCache(String name) {
		Assert.hasText(name);
		
		if (caches.get(name) == null)
			caches.putIfAbsent(name, new ConcurrentHashMap<Object,Object>());
		
		return caches.get(name);
	}
}
