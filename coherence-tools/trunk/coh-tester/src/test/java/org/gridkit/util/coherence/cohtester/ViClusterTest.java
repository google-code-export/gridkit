package org.gridkit.util.coherence.cohtester;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.DefaultCacheServer;
import com.tangosol.net.NamedCache;

public class ViClusterTest {

	@Rule
	public CohCloudRule cloud = new DisposableCohCloud();
	
	@Test
	public void failoverTest() throws InterruptedException {
		
		cloud.all().enableFastLocalCluster();
		cloud.all().enableJmx();
		
		
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

		cloud.all().enableFastLocalCluster();
		cloud.all().enableJmx();

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
