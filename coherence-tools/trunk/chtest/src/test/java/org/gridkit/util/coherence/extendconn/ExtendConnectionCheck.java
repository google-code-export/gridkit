package org.gridkit.util.coherence.extendconn;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import junit.framework.Assert;

import org.gridkit.coherence.chtest.CohCloudRule;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.vicluster.ViNode;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationService;
import com.tangosol.net.NamedCache;

public class ExtendConnectionCheck {

	public CohCloudRule cloud = new DisposableCohCloud();
	
	public static void main(String[] args) throws Exception {
		new ExtendConnectionCheck().test_cqc_memory_leak();
	}

	@Test
	public void test_cqc_memory_leak() throws Exception {
		
		final String cacheName = "test";
		
		// cluster member role
		cloud.node("cluster.**")
			.fastLocalClusterPreset()
			.autoStartCluster()
			.cacheConfig("/extend-server-cache-config.xml");

		// storage enabled for storage role
		cloud.node("cluster.storage.**")
			.localStorage(true);

		// storage enabled for proxy role
		cloud.node("cluster.proxy.**")
			.localStorage(false);
		
		// Extend client role
		cloud.node("xclient.**")
			.fastLocalClusterPreset()
			.cacheConfig("/extend-client-cache-config.xml");
		
		try {
			
			String[] cluster = {
					"cluster.storage.1",
					"cluster.storage.2",
					"cluster.proxy.1",					
			};
			
			// declare cluster nodes and start them
			cloud.nodes(cluster).touch();
			
			// ensure cache/cache service on all nodes
			cloud.nodes("cluster.**").getCache(cacheName);
			
			// start proxy service on proxies
			cloud.nodes("cluster.proxy.**")
				.ensureService("ExtendTcpProxyService");
			
			
			// check that cache is writeable
			cloud.node("cluster.proxy.1").getCache(cacheName).put("A", "A");

			// define client instance
			CohNode client = cloud.node("xclient.1");

			// verify connection to remote cache
			Assert.assertEquals("A", client.getCache(cacheName).get("A"));
			
			client.exec(new Runnable() {
				@Override
				public void run() {
					
					// TODO code is used only as invokation framework
					// few asserts should be added
					ExtendConnection con1 = new ExtendConnection("/extend-client-cache-config.xml");
					ExtendConnection con2 = new ExtendConnection("/extend-client-cache-config.xml");
					
					con1.getCache(cacheName);
					con2.getCache(cacheName);
					con2.getCache(cacheName);
					con1.getInvocationService("ExtendInvocation");
					
					con1.disconnect();
					con2.disconnect();
					
				}
			});
		}
		catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@SuppressWarnings("unused")
	private final class EventProducer extends Thread {
		private final String cacheName;
		private final ViNode storage;
	
		private EventProducer(String cacheName, ViNode storage) {
			this.cacheName = cacheName;
			this.storage = storage;
		}
	
		@Override
		public void run() {
			
			System.out.println("EventProducer started");
			
			final String cacheName = this.cacheName;
			
			storage.exec(new Runnable() {
				
				@Override
				public void run() {
					Random rnd = new Random();
					NamedCache cache = CacheFactory.getCache(cacheName);
					String payload = new String(new byte[500]);
					while(true) {
						for(int i = 0; i != 100; ++i) {
							Map<String, String> map = new HashMap<String, String>();
							map.put(Long.toBinaryString(rnd.nextInt(20000)), payload);
							map.put(Long.toBinaryString(rnd.nextInt(20000)), payload);
							map.put(Long.toBinaryString(rnd.nextInt(20000)), payload);
							map.put(Long.toBinaryString(rnd.nextInt(20000)), payload);
							map.put(Long.toBinaryString(rnd.nextInt(20000)), payload);
							map.put(Long.toBinaryString(rnd.nextInt(20000)), payload);
							map.put(Long.toBinaryString(rnd.nextInt(20000)), payload);
							cache.putAll(map);
						}
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							return;
						}
					}
				}
			});
			
		}
	}
	
	@SuppressWarnings("serial")
	public static class Task implements Invocable, Serializable {

		@Override
		public void init(InvocationService paramInvocationService) {
		}

		@Override
		public void run() {
		}

		@Override
		public Object getResult() {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
