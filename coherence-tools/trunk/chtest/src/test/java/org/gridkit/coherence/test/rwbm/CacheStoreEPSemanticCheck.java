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
package org.gridkit.coherence.test.rwbm;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;

import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.coherence.chtest.CohCloudRule;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

public class CacheStoreEPSemanticCheck {

	@Rule
	public CohCloudRule cloud = new DisposableCohCloud();
	
	@Test
	public void test_simple_store() {
		test_cache_store("simple-A");
	}
	
	public void test_cache_store(final String cacheName) {
		
		cloud.all().presetFastLocalCluster();
		cloud.all().cacheConfig("/cache-store-cache-config.xml");
		
		CohNode storage = cloud.node("storage");
		storage.localStorage(true);
		storage.autoStartServices();
		
		
		CohNode client = cloud.node("client");
		client.localStorage(false);
		
		cloud.all().getCache(cacheName);
		
		client.exec(new Callable<Void>(){
			@Override
			public Void call() throws Exception {
				
				NamedCache cache = CacheFactory.getCache(cacheName);
				Object old = cache.put("a", "AAA");
				System.out.println("Old a: " + old);
				cache.putAll(Collections.singletonMap("x", "y"));
//					cache.put("b", "BBB");
//					cache.put("c", "CCC");
//					cache.put("d", "DDD");
				
				cache.invokeAll(Arrays.asList("123", "456"), new SimpleEP());
				
				return null;
			}
		});
	}
	
	@SuppressWarnings("serial")
	public class SimpleEP extends AbstractProcessor implements Serializable {

		@Override
		public Object process(Entry e) {
			System.out.println("Key:" + e.getKey() + ", present:" + e.isPresent());
//			System.out.println("Key:" + e.getKey() + ", present:" + e.isPresent() + ", value:" + e.getValue());
//			e.setValue("Test");
			e.setValue("Test", true);
			return null;
		}
		
	}
}
