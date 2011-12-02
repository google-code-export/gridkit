package org.gridkit.coherence.util.vicluster;

import org.gridkit.utils.vicluster.CohHelper;
import org.gridkit.utils.vicluster.ViCluster;
import org.junit.Assert;
import org.junit.Test;

import com.tangosol.net.DefaultCacheServer;
import com.tangosol.net.NamedCache;

public class PermLeakTest {

	@Test
	public void permLeakShutdown() throws InterruptedException {
		
		for(int i = 0; i != 10; ++i) {
		
			ViCluster cluster = new ViCluster("permLeakShutdown-" + i, "com.tangosol", "org.gridkit");
			CohHelper.enableFastLocalCluster(cluster);
			CohHelper.enableJmx(cluster);
			
			cluster.node("server1").start(DefaultCacheServer.class);
			// ensure cache service
			cluster.node("server1").getCache("distr-A");
			
			CohHelper.localstorage(cluster.node("client"), false);
			
			NamedCache cache = cluster.node("client").getCache("distr-A");
			String cacheService = cluster.node("client").getServiceNameForCache("distr-A"); 
			
			cache.put("A", "A");
	
			System.out.println("Client node ID " + CohHelper.jmxMemberId(cluster.node("client")));
			System.out.println("Server node ID " + CohHelper.jmxMemberId(cluster.node("server1")));
			System.out.println("Server statusHA " + CohHelper.jmxServiceStatusHA(cluster.node("server1"), cacheService));
	
			Assert.assertEquals("Server's cache service is running", true, CohHelper.jmxServiceRunning(cluster.node("server1"), cacheService));
			
			
			cluster.node("server2").start(DefaultCacheServer.class);
			CohHelper.jmxWaitForService(cluster.node("server2"), cacheService);
			cluster.node("server1").shutdown();
			
			Assert.assertEquals("Value at 'A'", "A", cache.get("A"));
			
			cache = null;
			cluster.shutdown();
			cluster = null;
		}
	}	

	@Test
	public void permLeakKill() throws InterruptedException {
		
		for(int i = 0; i != 10; ++i) {
			
			ViCluster cluster = new ViCluster("permLeakKill-" + i, "com.tangosol", "org.gridkit");
			CohHelper.enableFastLocalCluster(cluster);
			CohHelper.enableJmx(cluster);
			
			cluster.node("server1").start(DefaultCacheServer.class);
			// ensure cache service
			cluster.node("server1").getCache("distr-A");
			
			CohHelper.localstorage(cluster.node("client"), false);
			
			NamedCache cache = cluster.node("client").getCache("distr-A");
			String cacheService = cluster.node("client").getServiceNameForCache("distr-A"); 
			
			cache.put("A", "A");
			
			System.out.println("Client node ID " + CohHelper.jmxMemberId(cluster.node("client")));
			System.out.println("Server node ID " + CohHelper.jmxMemberId(cluster.node("server1")));
			System.out.println("Server statusHA " + CohHelper.jmxServiceStatusHA(cluster.node("server1"), cacheService));
			
			Assert.assertEquals("Server's cache service is running", true, CohHelper.jmxServiceRunning(cluster.node("server1"), cacheService));
			
			
			cluster.node("server2").start(DefaultCacheServer.class);
			CohHelper.jmxWaitForService(cluster.node("server2"), cacheService);
			cluster.node("server1").shutdown();
			
			Assert.assertEquals("Value at 'A'", "A", cache.get("A"));
			
			cache = null;
			cluster.kill();
			cluster = null;
		}
	}	
}
