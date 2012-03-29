package org.gridkit.coherence.test.rwbm;

import java.util.Collection;
import java.util.Map;

import com.tangosol.net.cache.CacheStore;

public class SimpleCacheStore implements CacheStore {

	@Override
	public Object load(Object paramObject) {
		return null;
	}

	@Override
	public Map loadAll(Collection paramCollection) {
		return null;
	}

	@Override
	public void store(Object key, Object value) {
		System.out.println(key + " -> " + value);
	}

	@Override
	public void storeAll(Map paramMap) {
	}

	@Override
	public void erase(Object paramObject) {
	}

	@Override
	public void eraseAll(Collection paramCollection) {
	}
}
