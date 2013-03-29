package org.gridkit.coherence.test.rwbm;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.coherence.chtest.CohCloudRule;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;
import com.tangosol.util.MapTrigger;
import com.tangosol.util.filter.EqualsFilter;

public class DecoratorEventCheck {

	@Rule
	public CohCloudRule cloud = new DisposableCohCloud();
	
	@Test
	public void test_decorator_listener() {
		final String cacheName = "write-behind-A";
		
		cloud.all().presetFastLocalCluster();
		cloud.all().cacheConfig("/cache-store-cache-config.xml");
		
		CohNode storage = cloud.node("storage");
		storage.localStorage(true);
		
		CohNode client = cloud.node("client");
		client.localStorage(false);
		
		cloud.all().getCache(cacheName);
		
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
	
	@Test
	public void test_decorator_index() {
		final String cacheName = "write-behind-A";
		
		cloud.all().presetFastLocalCluster();
		cloud.all().cacheConfig("/cache-store-cache-config.xml");
		
		CohNode storage = cloud.node("storage");
		storage.localStorage(true);
		
		CohNode client = cloud.node("client");
		client.localStorage(false);
		
		cloud.all().getCache(cacheName);
		
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
			
				return null;
			}
		});
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

		@SuppressWarnings("unused")
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
