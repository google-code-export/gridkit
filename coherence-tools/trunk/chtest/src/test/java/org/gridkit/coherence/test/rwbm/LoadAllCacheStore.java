package org.gridkit.coherence.test.rwbm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.tangosol.net.cache.CacheStore;

public class LoadAllCacheStore implements CacheStore {

	public LoadAllCacheStore() {
		System.out.println("SimpleCacheStore created");
	}
	
	@Override
	public Object load(Object key) {		
		System.out.println("load:" + key);
		return key;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map loadAll(Collection keys) {
		System.out.println("loadAll: " + keys);
		Map result = new HashMap();
		for(Object key: keys) {
			if ("ALL".equals(key)) {
				keys.clear();
				keys.add("A");
				keys.add("B");
				keys.add("C");
				keys.add("D");
				result.clear();
				result.put("A", "aaa");
				result.put("B", "bbb");
				result.put("C", "ccc");
				result.put("D", "ddd");
				return result;
			}
			else {
				result.put(key, key);
			}			
		}
		return result;
	}

	@Override
	public void store(Object key, Object value) {
		System.out.println(key + " -> " + value);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void storeAll(Map paramMap) {
	}

	@Override
	public void erase(Object key) {
		System.out.println("erase: " + key);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void eraseAll(Collection paramCollection) {
	}
}
