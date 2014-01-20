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
package org.gridkit.coherence.check;

import java.util.Collections;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.coherence.chtest.CohCloudRule;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.aggregator.Count;
import com.tangosol.util.filter.AlwaysFilter;
import com.tangosol.util.processor.ConditionalPut;

public class LockSemanticCheck {

	@Rule
	public CohCloudRule cloud = new DisposableCohCloud();

	@Test(timeout = 30000)
	public void verify_invoke_ignores_lock() {
		
		final String cacheName = "dist-A";
		
		cloud.all().presetFastLocalCluster();
		cloud.all().cacheConfig("/coherence-cache-config.xml");
		
		CohNode storage = cloud.node("storage");
		storage.localStorage(true);
		storage.autoStartServices();
		
		storage.getCache(cacheName);
		
		cloud.node("client*").localStorage(false);
		
		CohNode client1 = cloud.node("client1");
		CohNode client2 = cloud.node("client2");
		
		cloud.all().touch();
		
		client1.exec(new Callable<Void>(){
			@Override
			public Void call() throws Exception {
				
				NamedCache cache = CacheFactory.getCache(cacheName);

				cache.lock("A");
				
				return null;
			}
		});

		client2.exec(new Callable<Void>(){
			@Override
			public Void call() throws Exception {
				
				NamedCache cache = CacheFactory.getCache(cacheName);
				
				cache.invoke("A", new ConditionalPut(AlwaysFilter.INSTANCE, "A"));
				
				return null;
			}
		});
	}
	
	@Test(timeout = 30000)
	public void verify_put_ignores_lock() {
		
		final String cacheName = "dist-A";
		
		cloud.all().presetFastLocalCluster();
		cloud.all().cacheConfig("/coherence-cache-config.xml");
		
		CohNode storage = cloud.node("storage");
		storage.localStorage(true);
		storage.autoStartServices();
		
		storage.getCache(cacheName);
		
		cloud.node("client*").localStorage(false);
		
		CohNode client1 = cloud.node("client1");
		CohNode client2 = cloud.node("client2");
		
		cloud.all().touch();
		
		client1.exec(new Callable<Void>(){
			@Override
			public Void call() throws Exception {
				
				NamedCache cache = CacheFactory.getCache(cacheName);

				Assert.assertTrue("Lock granted", cache.lock("A"));
				
				return null;
			}
		});

		client2.exec(new Callable<Void>(){
			@Override
			public Void call() throws Exception {
				
				NamedCache cache = CacheFactory.getCache(cacheName);
				
				cache.put("A", "A");
				
				return null;
			}
		});
	}		

	@Test(timeout = 30000)
	public void verify_aggregate_ignores_lock() {
		
		final String cacheName = "dist-A";
		
		cloud.all().presetFastLocalCluster();
		cloud.all().cacheConfig("/coherence-cache-config.xml");
		
		CohNode storage = cloud.node("storage");
		storage.localStorage(true);
		storage.autoStartServices();
		
		storage.getCache(cacheName);
		
		cloud.node("client*").localStorage(false);
		
		CohNode client1 = cloud.node("client1");
		CohNode client2 = cloud.node("client2");
		
		cloud.all().touch();
		
		client1.exec(new Callable<Void>(){
			@Override
			public Void call() throws Exception {
				
				NamedCache cache = CacheFactory.getCache(cacheName);
				
				Assert.assertTrue("Lock granted", cache.lock("A"));
				
				return null;
			}
		});
		
		client2.exec(new Callable<Void>(){
			@Override
			public Void call() throws Exception {
				
				NamedCache cache = CacheFactory.getCache(cacheName);
				
				cache.aggregate(Collections.singleton("A"), new Count());
				
				return null;
			}
		});
	}		

	@Test(timeout = 30000)
	public void verify_lock_exclusive() {
		
		final String cacheName = "dist-A";
		
		cloud.all().presetFastLocalCluster();
		cloud.all().cacheConfig("/coherence-cache-config.xml");
		
		CohNode storage = cloud.node("storage");
		storage.localStorage(true);
		storage.autoStartServices();
		
		storage.getCache(cacheName);
		
		cloud.node("client*").localStorage(false);
		
		CohNode client1 = cloud.node("client1");
		CohNode client2 = cloud.node("client2");
		
		cloud.all().touch();
		
		client1.exec(new Callable<Void>(){
			@Override
			public Void call() throws Exception {
				
				NamedCache cache = CacheFactory.getCache(cacheName);
				
				Assert.assertTrue("Lock granted", cache.lock("A"));
				
				return null;
			}
		});
		
		client2.exec(new Callable<Void>(){
			@Override
			public Void call() throws Exception {
				
				NamedCache cache = CacheFactory.getCache(cacheName);
				
				Assert.assertFalse("Lock rejected", cache.lock("A"));
				
				return null;
			}
		});
	}		
}
