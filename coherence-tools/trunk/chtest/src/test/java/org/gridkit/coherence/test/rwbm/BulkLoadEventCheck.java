package org.gridkit.coherence.test.rwbm;

import java.io.Serializable;
import java.util.Arrays;
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

public class BulkLoadEventCheck {

	@Rule
	public CohCloudRule cloud = new DisposableCohCloud();
	
	@Test
	public void test_decorator_listener() {
		final String cacheName = "load-all-A";

		cloud.all().presetFastLocalCluster();
		cloud.all().cacheConfig("/cache-store-cache-config.xml");

		
		cloud.node("storage*")
			.localStorage(true);
		
		for(int i = 1; i <= 3; ++i) {
			cloud.node("storage" + 1);
		}

		
		CohNode client = cloud.node("client");
		client.localStorage(false);
		
		cloud.all().getCache(cacheName);
		
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
