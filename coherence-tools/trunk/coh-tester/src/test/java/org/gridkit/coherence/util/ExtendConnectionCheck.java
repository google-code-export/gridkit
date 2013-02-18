package org.gridkit.coherence.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.gridkit.utils.vicluster.CohHelper;
import org.gridkit.utils.vicluster.ViCluster;
import org.gridkit.utils.vicluster.ViNode;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationService;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.ContinuousQueryCache;
import com.tangosol.util.filter.AlwaysFilter;

public class ExtendConnectionCheck {

	static {
		DefaultConfigurableCacheFactory.class.toString();
	}
	
	public static void main(String[] args) throws Exception {
		new ExtendConnectionCheck().test_cqc_memory_leak();
	}

	@Test
	public void test_cqc_memory_leak() throws Exception {
		
		final String cacheName = "test";
		
		ViCluster cluster = new ViCluster("test_cluster", "org.gridkit", "com.tangosol");
		ViCluster remote = new ViCluster("test_client", "org.gridkit", "com.tangosol");
		try {
			
			CohHelper.enableFastLocalCluster(cluster);
			CohHelper.cacheConfig(cluster, "/extend-server-cache-config.xml");
			
			final ViNode storage1 = cluster.node("storage1");
			final ViNode storage2 = cluster.node("storage2");
			CohHelper.localstorage(storage1, true);			
			CohHelper.localstorage(storage2, true);			
			storage1.getCache(cacheName);
			storage2.getCache(cacheName);
			
			ViNode proxy = cluster.node("proxy");
			CohHelper.localstorage(proxy, false);			
			proxy.getCache(cacheName);
			proxy.getService("ExtendTcpProxyService");
			
			proxy.getCache(cacheName).put("A", "A");
			
			CohHelper.enableFastLocalCluster(remote);
			CohHelper.cacheConfig(remote, "/extend-client-cache-config.xml");

			ViNode client1 = remote.node("client1");
			client1.getCache(cacheName);

			client1.getCache(cacheName).get("A");
			client1.exec(new Runnable() {
				@Override
				public void run() {
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
		finally {
			cluster.shutdown();
			remote.shutdown();
		}		
	}

	private final static class CQCClient implements Runnable, Serializable {

		private final String cacheName;

		private CQCClient(String cacheName) {
			this.cacheName = cacheName;
		}

		@Override
		public void run() {
			NamedCache cache = CacheFactory.getCache(cacheName);
			ContinuousQueryCache cqc1 = new ContinuousQueryCache(cache, AlwaysFilter.INSTANCE);
			ContinuousQueryCache cqc2 = new ContinuousQueryCache(cache, AlwaysFilter.INSTANCE);
			ContinuousQueryCache cqc3 = new ContinuousQueryCache(cache, AlwaysFilter.INSTANCE);
			
			System.out.println("CQC is started");
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				return;
			}
			
			System.out.println("CQC1 size " + cqc1.size());
			System.out.println("CQC2 size " + cqc2.size());
			System.out.println("CQC3 size " + cqc3.size());
			
			System.out.println("Shutdown cluster");
			
//			CacheFactory.shutdown();
		}
	}

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
