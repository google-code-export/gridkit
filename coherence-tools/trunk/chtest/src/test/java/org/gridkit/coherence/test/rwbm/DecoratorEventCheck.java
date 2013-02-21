package org.gridkit.coherence.test.rwbm;

import java.io.Serializable;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.gridkit.util.coherence.cohtester.CohHelper;
import org.gridkit.utils.vicluster.ViCluster;
import org.gridkit.utils.vicluster.ViNode;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultCacheServer;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;
import com.tangosol.util.MapTrigger;
import com.tangosol.util.MapTrigger.Entry;
import com.tangosol.util.filter.EqualsFilter;

public class DecoratorEventCheck {

	static {
		DefaultConfigurableCacheFactory.class.toString();
	}
	
	@Test
	public void test_decorator_listener() {
		final String cacheName = "write-behind-A";
		
		ViCluster cluster = new ViCluster("test_decorator_listener", "org.gridkit", "com.tangosol");
		try {
			
			CohHelper.enableFastLocalCluster(cluster);
			CohHelper.cacheConfig(cluster, "/cache-store-cache-config.xml");
			
			ViNode storage = cluster.node("storage");
			CohHelper.localstorage(storage, true);
			
			storage.getCache(cacheName);
			
			ViNode client = cluster.node("client");
			CohHelper.localstorage(client, false);
			
			client.exec(new Callable<Void>(){
				@Override
				public Void call() throws Exception {
					
					NamedCache cache = CacheFactory.getCache(cacheName);
					cache.addMapListener(new EntryListener());
					
					cache.put("A", "aaaa");
					System.out.println("A <- aaaa");
					System.out.println("not-yet-stored " + cache.keySet(new EqualsFilter(new StoreFlagExtractor(), Boolean.FALSE)));
					
					Thread.sleep(5000);
					System.out.println("not-yet-stored " + cache.keySet(new EqualsFilter(new StoreFlagExtractor(), Boolean.FALSE)));
					
					return null;
				}
			});
		}
		finally {
			cluster.shutdown();
		}		
	}

	
	@Test
	public void test_decorator_index() {
		final String cacheName = "write-behind-A";
		
		ViCluster cluster = new ViCluster("test_decorator_listener", "org.gridkit", "com.tangosol");
		try {
			
			CohHelper.enableFastLocalCluster(cluster);
			CohHelper.cacheConfig(cluster, "/cache-store-cache-config.xml");
			
			ViNode storage1 = cluster.node("storage");
//			ViNode storage2 = cluster.node("storage2");
			CohHelper.localstorage(storage1, true);
//			CohHelper.localstorage(storage2, true);
			
			storage1.getCache(cacheName);
//			storage2.getCache(cacheName);
			
			ViNode client = cluster.node("client");
			CohHelper.localstorage(client, false);
			
			client.exec(new Callable<Void>(){
				@Override
				public Void call() throws Exception {
					
					NamedCache cache = CacheFactory.getCache(cacheName);
					
//					for(int i = 0; i != 1000; ++i) {
//						cache.put(i, i);
//					}
//					
//					cache.addMapListener(new EntryListener("test"));
					
					cache.addIndex(new StoreFlagExtractor(), false, null);
					
					cache.put("A", "aaaa");
					System.out.println("A <- aaaa");
					System.out.println("not-yet-stored " + cache.keySet(new EqualsFilter(new StoreFlagExtractor(), Boolean.FALSE)));
					System.out.println("...");
					
					Thread.sleep(5000);
					System.out.println("...");
					System.out.println("not-yet-stored " + cache.keySet(new EqualsFilter(new StoreFlagExtractor(), Boolean.FALSE)));
				
					Thread.sleep(10000000l);
					return null;
				}
			});
		}
		finally {
			try {
				cluster.shutdown();
			}
			catch(Exception e) {
				// ignore
			}
		}		
	}
	
	@SuppressWarnings("serial")
	public static class EntryListener implements MapListener, Serializable {

		private String marker = "default";
		
		public EntryListener() {
			System.out.println("Creating default listener");
		}
		
		public EntryListener(String marker) {
			this.marker = marker;
		}

		@Override
		public void entryInserted(MapEvent event) {
			System.out.println(marker + ": " + event);
		}

		@Override
		public void entryUpdated(MapEvent event) {
			System.out.println(marker + ": " + event);
		}

		@Override
		public void entryDeleted(MapEvent event) {
			System.out.println(marker + ": " + event);
		}
	}
	
	@SuppressWarnings("serial")
	public static class EntryTrigger implements MapTrigger, Serializable {

		private String marker = "default";
		
		public EntryTrigger() {
			System.out.println("Creating default trigger");
		}
		
		public EntryTrigger(String marker) {
			this.marker = marker;
		}

		@Override
		public void process(Entry entry) {
			if (entry.isOriginalPresent() && entry.isPresent()) {
				entry.setValue(entry.getValue().toString() + "-X");
			}
		}
	}	
}
