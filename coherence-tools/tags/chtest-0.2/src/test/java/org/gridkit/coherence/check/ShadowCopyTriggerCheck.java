package org.gridkit.coherence.check;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.coherence.chtest.CohCloudRule;
import org.gridkit.coherence.chtest.CohHelper;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.gridkit.coherence.test.CacheTemplate;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.BackingMapContext;
import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.CacheFactory;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;
import com.tangosol.util.MapTrigger;
import com.tangosol.util.MapTriggerListener;

public class ShadowCopyTriggerCheck {

	@Rule
	public CohCloudRule cloud = new DisposableCohCloud();
	private ExecutorService executor = Executors.newCachedThreadPool();

	@After
	public void shutdown_cluster_after_test() {
		executor.shutdownNow();
	}
	
	@Test
	public void test_shadow_copy_trigger() throws InterruptedException, ExecutionException {
		
		cloud.all().presetFastLocalCluster();
		
		CacheTemplate.useTemplateCacheConfig(cloud.all());
		CacheTemplate.usePartitionedInMemoryCache(cloud.all());
		CacheTemplate.usePartitionedCacheBackingMapListener(cloud.all(), BMListener.class);
		
		CohNode storage = cloud.node("storage");
		CohHelper.localstorage(storage, true);

		storage.exec(new Runnable() {
			
			@Override
			public void run() {
				CacheFactory.getCache("a-A").put("AAA", "aaa");
				CacheFactory.getCache("a-B").put("AAA", "___");
				CacheFactory.getCache("a-A").addMapListener(new MapTriggerListener(new CacheTrigger()));
				CacheFactory.getCache("a-A").put("BBB", "bbb");
			}
		});
		
		Assert.assertEquals("bbb", storage.getCache("a-B").get("BBB"));
	}

	
	@SuppressWarnings("serial")
	public static class CacheTrigger implements MapTrigger, Serializable {

		@SuppressWarnings("unchecked")
		@Override
		public void process(Entry e) {
			BinaryEntry be = (BinaryEntry)e;
			BackingMapContext map = be.getContext().getBackingMapContext("a-B");
			map.getBackingMap().put(be.getBinaryKey(), be.getBinaryValue());
		}
	}
	
	public static class BMListener implements MapListener {

		public BMListener(String cacheName, BackingMapManagerContext context) {
			System.out.println("Listener attached: " + cacheName);
		}
		
		@Override
		public void entryDeleted(MapEvent e) {
			System.out.println(e);			
		}

		@Override
		public void entryInserted(MapEvent e) {
			System.out.println(e);			
		}

		@Override
		public void entryUpdated(MapEvent e) {
			System.out.println(e);			
		}		
	}	
}
