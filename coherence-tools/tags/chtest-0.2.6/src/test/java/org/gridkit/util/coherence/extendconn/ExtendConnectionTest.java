package org.gridkit.util.coherence.extendconn;

import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.gridkit.coherence.chtest.CohHelper;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.ProxyService;

public class ExtendConnectionTest {

	@Rule
	public DisposableCohCloud cloud = new DisposableCohCloud();
	
	@Test
	public void verify_connection_number() throws Exception {
		
		final String cacheName = "cache";

		cloud.all().outOfProcess(true);
		
		cloud.node("server.**")
			.cacheConfig("extend-server-cache-config.xml")
			.presetFastLocalCluster();
		cloud.node("xclient.**")
			.cacheConfig("extend-client-cache-config.xml")
			.enableTCMP(false);
		
		cloud.node("server.1");
		cloud.node("xclient.1");

		cloud.node("**").enableJmx(true);
		cloud.node("server.**").ensureCluster();
		
		cloud.node("server.1").getCache(cacheName);
		cloud.node("server.1").ensureService("ExtendTcpProxyService");
		
		cloud.node("xclient.1").exec(new Runnable() {
			
			@Override
			public void run() {
				CacheFactory.getCache(cacheName);
				
				ExtendConnection conn1 = new ExtendConnection("extend-client-cache-config.xml");
				NamedCache cache1 = conn1.getCache(cacheName);
				
				for(int i = 0; i != 100; ++i) {
					cache1.put(i, i);
				}

				ExtendConnection conn2 = new ExtendConnection("extend-client-cache-config.xml");
				NamedCache cache2 = conn2.getCache(cacheName);

				for(int i = 0; i != 100; ++i) {
					cache2.put(i, i);
				}
			}
		});
		
		cloud.node("server.1").exec(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				
				int connections = CohHelper.getNumberOfConnections((ProxyService) CacheFactory.getService("ExtendTcpProxyService"));
				
				// 1 connection from cache factory
				// 2 others are created explicitly
				Assert.assertEquals(3, connections);
				
				return null;
			}
		});
	}
}
