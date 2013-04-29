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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.gridkit.coherence.chtest.CohCloudRule;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.AbstractCacheLoader;
import com.tangosol.net.cache.CacheLoader;
import com.tangosol.net.cache.LocalCache;
import com.tangosol.util.Binary;
import com.tangosol.util.MapListener;
import com.tangosol.util.MapTrigger;
import com.tangosol.util.MapTriggerListener;

public class CacheLoaderCheck {

	@Test
	public void test_cache_loader__no_miss_cache() {
		test_cache_loader("vanila-A");
	}

	@Test
	public void test_cache_loader__miss_cache() {
		test_cache_loader("miss-A");
	}

	@Test
	public void test_cache_loader__null_removal_trigger() {
		test_cache_loader("nt-A");
	}

	@Test
	public void test_cache_loader__null_filter_map() {
		test_cache_loader("nf-A");
	}
	
	@Rule
	public CohCloudRule cloud = new DisposableCohCloud();
	
	public void test_cache_loader(final String cacheName) {
		
		cloud.node("**")
			.presetFastLocalCluster()
			.cacheConfig("/cache-loader-cache-config.xml");
		
		CohNode storage = cloud.node("storage");
		storage.localStorage(true);
		storage.autoStartServices();
		
		CohNode client = cloud.node("client");
		client.localStorage(false);

		// Trigger cluster instantiation by asking for a cache
		cloud.node("**").getCache(cacheName);
		
		
		client.exec(new Callable<Void>(){
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public Void call() throws Exception {
				
				NamedCache cache = CacheFactory.getCache(cacheName);
				
				List<Integer> keys = Arrays.asList(1,2,3,4,5,6,7);

				cache.put(0, 0);
				
				TreeMap result = new TreeMap(cache.getAll(keys));
				
				System.out.println("getAll -> " + result);

				result = new TreeMap(cache.getAll(keys));
				
				System.out.println("getAll -> " + result);
				
				
				return null;
			}
		});
	}
	
	
	@SuppressWarnings("serial")
	public static class NullRemovingTrigger implements MapTrigger, Serializable {
		
		public static MapListener create() {
			System.out.println("Create trigger");
			return new MapTriggerListener(new NullRemovingTrigger());
		}

		@Override
		public void process(Entry entry) {
			System.out.println("Trigger event " + entry);
			if (entry.getValue() == null) {
				entry.remove(true);
			}
		}		
	}
	
	public static class OddLoader extends AbstractCacheLoader {
		@Override
		public Object load(Object key) {
			if (key instanceof Integer) {
				if (((Integer)key).intValue() % 2 == 1) {
					return key;
				}
			}
			return null;
		}
	}
	
	@SuppressWarnings("serial")
	public static class NullRemovingLocalCache extends LocalCache {

		private static Binary NULL = new Binary(new byte[]{0});
		private static Binary POF_NULL = new Binary(new byte[]{21, 100});
		
		public NullRemovingLocalCache() {
			super();
		}

		public NullRemovingLocalCache(int cUnits, int cExpiryMillis,
				CacheLoader loader) {
			super(cUnits, cExpiryMillis, loader);
		}

		public NullRemovingLocalCache(int cUnits, int cExpiryMillis) {
			super(cUnits, cExpiryMillis);
		}

		public NullRemovingLocalCache(int cUnits) {
			super(cUnits);
		}

		@Override
		@SuppressWarnings("deprecation")
		public Object put(Object oKey, Object oValue) {
			if (NULL.equals(oValue) || POF_NULL.equals(oValue)) {
				// ignoring nulls
				return null;
			}
			return super.put(oKey, oValue);
		}

		@Override
		@SuppressWarnings("deprecation")
		public Object put(Object oKey, Object oValue, long cMillis) {
			if (NULL.equals(oValue) || POF_NULL.equals(oValue)) {
				// ignoring nulls
				return null;
			}
			return super.put(oKey, oValue, cMillis);
		}
	}
}
