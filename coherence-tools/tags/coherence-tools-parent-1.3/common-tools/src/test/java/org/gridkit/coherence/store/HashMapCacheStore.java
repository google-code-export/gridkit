package org.gridkit.coherence.store;

import java.util.HashMap;
import java.util.Map;

import com.tangosol.net.cache.MapCacheStore;

public class HashMapCacheStore extends MapCacheStore {
	public HashMapCacheStore() {
		super(new HashMap<Object, Object>());
	}
	
	@Override
	public void storeAll(@SuppressWarnings("rawtypes") Map mapEntries) {
		super.storeAll(mapEntries);
	}
}
