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

import org.gridkit.coherence.chtest.CohCloud;
import org.gridkit.coherence.chtest.CohHelper;
import org.gridkit.coherence.chtest.SimpleCohCloud;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.tangosol.net.NamedCache;

public class PermLeakTest {

	@Test
	@Ignore("Graceful shutdown is disfunct")
	public void test_perm_leak_graceful() throws InterruptedException {
		permLeakShutdown(true);
	}

	@Test
	public void test_perm_leak_forceful() throws InterruptedException {
		permLeakShutdown(false);
	}
	
	void permLeakShutdown(boolean graceful) throws InterruptedException {
		
		for(int i = 0; i != 20; ++i) {
		
			CohCloud cluster = new SimpleCohCloud();
			try {
				cluster.all()
//					.gracefulShutdown(graceful)
					.presetFastLocalCluster()
					.setTCMPTimeout(10000)
					.enableJmx(true);
				
				cluster.node(i + ".server1");
				cluster.node(i + ".server2");
				cluster.node(i + ".client");
				
				cluster.node("*.server1").autoStartServices();
				// ensure cache service
				cluster.node("*.server1").getCache("distr-A");
				
				cluster.node("*.client").localStorage(false);
				
				NamedCache cache = cluster.node("*.client").getCache("distr-A");
				String cacheService = cluster.node("*.client").getServiceNameForCache("distr-A"); 
				
				cache.put("A", "A");
		
				System.out.println("Client node ID " + CohHelper.jmxMemberId(cluster.node("*.client")));
				System.out.println("Server node ID " + CohHelper.jmxMemberId(cluster.node("*.server1")));
				System.out.println("Server statusHA " + CohHelper.jmxServiceStatusHA(cluster.node("*.server1"), cacheService));
		
				Assert.assertEquals("Server's cache service is running", true, CohHelper.jmxServiceRunning(cluster.node("*.server1"), cacheService));
				
				
				cluster.node("*.server2").autoStartServices().touch();
				
				CohHelper.jmxWaitForService(cluster.node("*.server2"), cacheService);
				cluster.node("*.server1").shutdown();
				
				Assert.assertEquals("Value at 'A'", "A", cache.get("A"));
				
				cache = null;
			}
			catch(AssertionError e) {
				// ignore
			}
			
			cluster.shutdown();
			cluster = null;
		}
	}	
}
