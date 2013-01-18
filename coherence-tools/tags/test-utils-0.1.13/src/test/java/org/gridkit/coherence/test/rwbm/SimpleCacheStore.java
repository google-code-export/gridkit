package org.gridkit.coherence.test.rwbm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.tangosol.net.cache.CacheStore;

public class SimpleCacheStore implements CacheStore {

	public SimpleCacheStore() {
		System.out.println("SimpleCacheStore created");
	}
	
	@Override
	public Object load(Object key) {
		System.out.println("load:" + key);
		return key;
	}

	@Override
	public Map loadAll(Collection keys) {
		System.out.println("loadAll: " + keys);
		Map result = new HashMap();
		for(Object key: keys) {
			result.put(key, key);
		}
		return result;
	}

	@Override
	public void store(Object key, Object value) {
		System.out.println(key + " -> " + value);
	}

	@Override
	public void storeAll(Map paramMap) {
	}

	@Override
	public void erase(Object key) {
		System.out.println("erase: " + key);
	}

	@Override
	public void eraseAll(Collection paramCollection) {
	}
}
