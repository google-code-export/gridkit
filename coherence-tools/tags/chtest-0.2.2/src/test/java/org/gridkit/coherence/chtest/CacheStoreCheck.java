/**
 * Copyright 2013 Alexey Ragozin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gridkit.coherence.chtest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.gridkit.coherence.chtest.CohCloudRule;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.AbstractCacheStore;

public class CacheStoreCheck {

	@Test
	public void test_cache_store() {
		test_cache_store("vanila-A");
	}

	@Rule
	public CohCloudRule cloud = new DisposableCohCloud();
	
	public void test_cache_store(final String cacheName) {
		
		cloud.node("**")
			.presetFastLocalCluster()
			.cacheConfig("/cache-store-cache-config.xml");
		
		cloud.node("**").setProp("test-cache-store-class", TestStore.class.getName());
			

		CohNode storage = cloud.node("storage");
		storage.localStorage(true);
		storage.autoStartServices();
		
		CohNode client = cloud.node("client");
		client.localStorage(false);
		
		cloud.node("**").getCache(cacheName);
		
		client.exec(new Callable<Void>(){
			@Override
			public Void call() throws Exception {
				
				NamedCache cache = CacheFactory.getCache(cacheName);
				
				Map<Integer, String> values = new HashMap<Integer, String>();
				values.put(0, "0");
				values.put(1, "1");
				values.put(2, "2");
				values.put(3, "3");
				values.put(4, "4");
				values.put(5, "5");
				values.put(6, "6");

				cache.putAll(values);					
				
				return null;
			}
		});
	}	
	
	public static class TestStore extends AbstractCacheStore {

		@Override
		public void store(Object oKey, Object oValue) {
			System.out.println("store: " + oKey + " -> " + oValue);
		}

		@Override
		@SuppressWarnings("rawtypes")
		public void storeAll(Map value) {
			System.out.println("storeAll: " + value );
		}

		@Override
		public Object load(Object key) {
			return null;
		}
	}
}
