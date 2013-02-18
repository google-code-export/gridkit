package org.gridkit.coherence.test.rwbm;

import java.io.Serializable;
import java.util.Arrays;
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

public class BulkLoadEventCheck {

	static {
		DefaultConfigurableCacheFactory.class.toString();
	}
	
	@Test
	public void test_decorator_listener() {
		final String cacheName = "load-all-A";
		
		ViCluster cluster = new ViCluster("test_decorator_listener", "org.gridkit", "com.tangosol");
		try {
			
			CohHelper.enableFastLocalCluster(cluster);
			CohHelper.cacheConfig(cluster, "/cache-store-cache-config.xml");
			
			ViNode storage1 = cluster.node("storage1");
			ViNode storage2 = cluster.node("storage2");
			ViNode storage3 = cluster.node("storage3");
			CohHelper.localstorage(storage1, true);
			CohHelper.localstorage(storage2, true);
			CohHelper.localstorage(storage3, true);
			
			storage1.getCache(cacheName);
			storage2.getCache(cacheName);
			storage3.getCache(cacheName);
			
			ViNode client = cluster.node("client");
			CohHelper.localstorage(client, false);
			
			client.exec(new Callable<Void>(){
				@Override
				public Void call() throws Exception {
					
					NamedCache cache = CacheFactory.getCache(cacheName);
					cache.addMapListener(new EntryListener());
					
					cache.getAll(Arrays.asList("ALL", "ALLALL"));

					System.out.println("A -> " + cache.get("A"));
					System.out.println("B -> " + cache.get("B"));
					System.out.println("C -> " + cache.get("C"));
					System.out.println("D -> " + cache.get("D"));
					
					return null;
				}
			});
		}
		finally {
			cluster.shutdown();
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
