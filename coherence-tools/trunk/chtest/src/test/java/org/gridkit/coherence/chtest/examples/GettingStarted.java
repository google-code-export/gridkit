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
package org.gridkit.coherence.chtest.examples;

import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class GettingStarted {
	
	@Rule
	public DisposableCohCloud cloud = new DisposableCohCloud();
	
	@SuppressWarnings("unused")
	@Test
	public void simple_cluster() {
		
		// Present for typical single node cluster
		cloud.all().presetFastLocalCluster();
		
		cloud.node("storage.**").localStorage(true);
		cloud.node("client.**").localStorage(false);
		
		// Simulates DefaultCacheServer based process
		cloud.node("storage.**").autoStartServices();
		
		// declaring specific nodes to be created
		CohNode storage = cloud.node("storage.1");
		CohNode client1 = cloud.node("client.1");
		CohNode client2 = cloud.node("client.2");
		
		// now we have 3 specific nodes in cloud
		// all of then will be initialized in parallel
		cloud.all().ensureCluster();

		final String cacheName = "distr-a";
		
		client1.exec(new Runnable() {
			@Override
			public void run() {
				NamedCache cache = CacheFactory.getCache(cacheName);
				Assert.assertNull(cache.get("A"));
				cache.put("A", "aaa");
			}
		});
		
		client2.exec(new Runnable() {
			@Override
			public void run() {
				NamedCache cache = CacheFactory.getCache(cacheName);
				Assert.assertEquals("aaa", cache.get("A"));
			}
		});
	}
}
