package org.gridkit.coherence.util.vicluster;

import java.lang.management.ManagementFactory;

import junit.framework.Assert;

import org.gridkit.utils.vicluster.CohHelper;
import org.gridkit.utils.vicluster.ViCluster;
import org.junit.Test;

import com.tangosol.net.DefaultCacheServer;
import com.tangosol.net.NamedCache;

public class ViClusterTest {

	@Test
	public void failoverTest() throws InterruptedException {
		
		for(int i = 0; i != 1000; ++i) {
			ViCluster cluster = new ViCluster("failoverTest-" + i, "com.tangosol", "org.gridkit");
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
			cluster.node("server2").getCluster();
			CohHelper.jmxWaitForService(cluster.node("server2"), cacheService);
			System.out.println("Shutting down server1");
			cluster.node("server1").shutdown();
			
			Assert.assertEquals("Value at 'A'", "A", cache.get("A"));
			
			cluster.kill();	
			System.out.println("Cluster is down");
			cluster = null;
			System.gc();
			System.gc();
			System.gc();
			System.gc();
			System.gc();
		}		
		
		Thread.sleep(1000000);
	}
	
	@Test
	public void HAStatusTest() throws InterruptedException {

		ViCluster cluster = new ViCluster("HAStatusTest", "com.tangosol", "org.gridkit");
		CohHelper.enableFastLocalCluster(cluster);
		CohHelper.enableJmx(cluster);

		cluster.node("server1").start(DefaultCacheServer.class);
		cluster.node("server2").start(DefaultCacheServer.class);
		
		CohHelper.localstorage(cluster.node("client"), false);
		
		String cacheService = cluster.node("client").getServiceNameForCache("distr-A"); 

		CohHelper.jmxWaitForStatusHA(cluster.node("client"), cacheService, "NODE-SAFE");
		Assert.assertEquals("Service HA status", "NODE-SAFE", CohHelper.jmxServiceStatusHA(cluster.node("client"), cacheService));
		
		cluster.node("server1").kill();
		Thread.sleep(1000);
		
		Assert.assertEquals("Service HA status", "ENDANGERED", CohHelper.jmxServiceStatusHA(cluster.node("client"), cacheService));
		
		cluster.node("server3").start(DefaultCacheServer.class);
		
		CohHelper.jmxWaitForStatusHA(cluster.node("client"), cacheService, "NODE-SAFE");

		cluster.shutdown();
	}
	
}
