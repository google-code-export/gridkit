package org.gridkit.coherence.store;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class BatchStoreUploaderApp {
	public static void main(String[] args) {
		System.setProperty("tangosol.coherence.cacheconfig", "test-cache-config.xml");
		System.setProperty("tangosol.coherence.localhost", "127.0.0.1");
		System.setProperty("tangosol.coherence.distributed.localstorage", "false");
		
		NamedCache cache = CacheFactory.getCache("store-cache");
		
		BatchStoreUploader uploader = new BatchStoreUploader(cache);
		uploader.put("1", "test");
		uploader.put("2", "test2");
		uploader.flush();
		
		System.out.println(cache.get("1"));
		System.out.println(cache.get("2"));
	}
}
