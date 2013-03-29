package org.gridkit.coherence.chtest.examples;

import java.util.concurrent.TimeUnit;

import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.NamedCache;

public class ExtendServerCheck {

	@Rule
	public DisposableCohCloud cloud = new DisposableCohCloud();
	
	@Test
	public void start_server() throws InterruptedException {
		
		cloud.all()
//			.pofConfig("extend-test-pof-config.xml")
			.pofEnabled(true);

		cloud.node("cluster.**")
			.cacheConfig("extend-server-cache-config.xml");		
			
		cloud.node("xclient.**")
			.enableTCMP(false)
			.cacheConfig("extend-client-cache-config.xml");

		
		cloud.node("cluster.storage.**")
			.autoStartServices()
			.localStorage(true);

		cloud.node("cluster.proxy.**")
			.autoStartServices()
			.localStorage(false);
	
		cloud.node("cluster.storage.1");
		cloud.node("cluster.proxy.1");
		
		cloud.all().getCache("cache");
		
		cloud.node("cluster.proxy.1").ensureService("ExtendTcpProxyService");
		
		NamedCache cache = cloud.node("cluster.proxy.1").getCache("cache");
		
		for(int i = 0; i != 1000; ++i) {
			cache.put("Key-" + i, i);
		}
		
		cloud.node("xclient.1").getCache("cache");
		
		System.out.println("Cache started");
		
		Thread.sleep(TimeUnit.HOURS.toMillis(2));
	}
}
