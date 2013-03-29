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

import junit.framework.Assert;

import org.gridkit.coherence.chtest.CohCloudRule;
import org.gridkit.coherence.chtest.CohHelper;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.NamedCache;

public class ViClusterTest {

	@Rule
	public CohCloudRule cloud = new DisposableCohCloud();
	
	@Test
	public void failoverTest() throws InterruptedException {
		
		cloud.all().presetFastLocalCluster();
		cloud.all().enableJmx(true);
		
		
		cloud.node("server*").autoStartServices();
		cloud.node("server*").localStorage(true);
		cloud.node("client*").localStorage(false);

		String cacheName = "distr-A";
		cloud.nodes("server1", "client").getCache(cacheName);
		
		// get instance of NamedCache from client
		NamedCache cache = cloud.nodes("client").getCache(cacheName);
		
		String cacheService = cloud.node("client").getServiceNameForCache(cacheName); 
		
		cache.put("A", "A");

		System.out.println("Client node ID " + CohHelper.jmxMemberId(cloud.node("client")));
		System.out.println("Server node ID " + CohHelper.jmxMemberId(cloud.node("server1")));
		System.out.println("Server statusHA " + CohHelper.jmxServiceStatusHA(cloud.node("server1"), cacheService));

		Assert.assertEquals("Server's cache service is running", true, CohHelper.jmxServiceRunning(cloud.node("server1"), cacheService));
		
		
		cloud.node("server2").touch();
		
		CohHelper.jmxWaitForService(cloud.node("server2"), cacheService);
		cloud.node("server1").shutdown();
		
		Assert.assertEquals("Value at 'A'", "A", cache.get("A"));
		
		cloud.shutdown();		
	}
	
	@Test
	public void HAStatusTest() throws InterruptedException {

		cloud.all().presetFastLocalCluster();
		cloud.all().enableJmx(true);

		cloud.node("server*").autoStartServices();
		cloud.node("server*").localStorage(true);
		cloud.node("cleint*").localStorage(false);
		
		String cacheName = "distr-A";
		
		cloud.nodes("server1", "server2", "client").getCache(cacheName);
		
		String cacheService = cloud.node("client").getServiceNameForCache("distr-A"); 

		CohHelper.jmxWaitForStatusHA(cloud.node("client"), cacheService, "NODE-SAFE");
		Assert.assertEquals("Service HA status", "NODE-SAFE", CohHelper.jmxServiceStatusHA(cloud.node("client"), cacheService));

		// Kill one server, now we have only one storage in cluster
		cloud.node("server1").shutdown();
		Thread.sleep(1000);
		
		// ENDANGERED status expected
		Assert.assertEquals("Service HA status", "ENDANGERED", CohHelper.jmxServiceStatusHA(cloud.node("client"), cacheService));
		
		// Bring up another storage node
		cloud.node("server3").touch();
		
		// wait for NODE-SAFE
		CohHelper.jmxWaitForStatusHA(cloud.node("client"), cacheService, "NODE-SAFE");
	}	
}
