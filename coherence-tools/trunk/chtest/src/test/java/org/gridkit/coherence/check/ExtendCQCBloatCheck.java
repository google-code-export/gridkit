package org.gridkit.coherence.check;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.coherence.chtest.CohCloudRule;
import org.gridkit.coherence.chtest.CohHelper;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.gridkit.vicluster.ViNode;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationService;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.ContinuousQueryCache;
import com.tangosol.util.filter.AlwaysFilter;

public class ExtendCQCBloatCheck {

	@Rule
	public CohCloudRule cloud = new DisposableCohCloud(); 
	
	public static void main(String[] args) throws Exception {
		new ExtendCQCBloatCheck().test_cqc_memory_leak();
	}

	@Test
	public void test_cqc_memory_leak() throws Exception {
		
		final String cacheName = "test";

		CohNode grid = cloud.node("grid.**");
		CohNode client = cloud.node("client.**");

		grid.presetFastLocalCluster();
		grid.cacheConfig("/extend-server-cache-config.xml");
		
		client.enableTCMP(false);
		client.cacheConfig("/extend-client-cache-config.xml");
		
		final CohNode storage1 = cloud.node("grid.storage1");
		final CohNode storage2 = cloud.node("grid.storage2");
		CohHelper.localstorage(storage1, true);			
		CohHelper.localstorage(storage2, true);			
		storage1.getCache(cacheName);
		storage2.getCache(cacheName);
		
		CohNode proxy = cloud.node("grid.proxy");
		CohHelper.localstorage(proxy, false);			
		proxy.getCache(cacheName);
		proxy.ensureService("ExtendTcpProxyService");
		
		proxy.getCache(cacheName).put("A", "A");
		
		CohNode client1 = cloud.node("client.1");
		client1.getCache(cacheName);

		client1.getCache(cacheName).get("A");
		client1.exec(new Runnable() {
			@Override
			public void run() {
				((InvocationService)CacheFactory.getService("ExtendInvocation")).query(new Task(), null);
			}
		});
		proxy.suspend();
		System.out.println("Proxy is suspended, wait client timeout");
		client1.exec(new Runnable() {
			@Override
			public void run() {
				((InvocationService)CacheFactory.getService("ExtendInvocation")).query(new Task(), null);
			}
		});
		client1.getCache(cacheName).get("A");
		System.out.println("Passed");
		
		CohNode client2 = cloud.node("client.2");
		client2.getCache(cacheName);
		CohNode client3 = cloud.node("client.3");
		client3.getCache(cacheName);
		
		for(int i = 0; i != 100; ++i) {
			new EventProducer(cacheName, proxy).start();
		}

		client1.exec(new CQCClient(cacheName));
		client2.exec(new CQCClient(cacheName));
		client3.exec(new CQCClient(cacheName));

		System.out.println("Killing remote");
		client1.suspend();
//			remote.kill();
		System.out.println("Client has been killed");
		
		Thread.sleep(600 * 1000);			
	}

	@SuppressWarnings("serial")
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
