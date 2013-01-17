package org.gridkit.coherence.check;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.gridkit.coherence.test.CacheTemplate;
import org.gridkit.utils.vicluster.CohHelper;
import org.gridkit.utils.vicluster.ViCluster;
import org.gridkit.utils.vicluster.ViNode;
import org.junit.After;
import org.junit.Test;

import com.tangosol.net.BackingMapContext;
import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.CacheFactory;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;
import com.tangosol.util.MapTrigger;
import com.tangosol.util.MapTriggerListener;

public class TriggerBMAccesCheck {

	private ViCluster cluster;
	private ExecutorService executor = Executors.newCachedThreadPool();

	@After
	public void shutdown_cluster_after_test() {
		cluster.shutdown();
		executor.shutdownNow();
	}
	
	@Test
	public void test_shadow_copy_trigger() throws InterruptedException, ExecutionException {
		
		cluster = new ViCluster("test", "org.gridkit", "com.tangosol");
		
		CacheTemplate.useTemplateCacheConfig(cluster);
		CacheTemplate.usePartitionedInMemoryCache(cluster);
		CacheTemplate.usePartitionedCacheBackingMapListener(cluster, BMListener.class);
		
		ViNode storage = cluster.node("storage");
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

	
	/* garbage code to investigate threading picture
	@Test
	public void __test_shadow_copy_trigger() throws InterruptedException, ExecutionException {
		
		cluster = new ViCluster("test", "org.gridkit", "com.tangosol");
		
		CacheTemplate.useTemplateCacheConfig(cluster);
		CacheTemplate.usePartitionedServiceThreadCount(cluster, 2);
		CacheTemplate.usePartitionedInMemoryCache(cluster);
		CacheTemplate.usePartitionedCacheBackingMapListener(cluster, BMListener.class);
		
		final ViNode storage = cluster.node("storage");
		CohHelper.localstorage(storage, true);
		
		storage.getCache("a-A");		

		executor.submit(new Runnable() {
			@Override
			public void run() {
				List<CompositeKey> keys = Arrays.asList(new CompositeKey("A", 1), new CompositeKey("A", 11));
				storage.getCache("a-A").invokeAll(keys, new ConditionalPut(AlwaysFilter.INSTANCE, "A"));
			}
		});

		executor.submit(new Runnable() {
			@Override
			public void run() {
				List<CompositeKey> keys = Arrays.asList(new CompositeKey("A", 1), new CompositeKey("A", 11));
				storage.getCache("a-A").invokeAll(keys, new ConditionalPut(AlwaysFilter.INSTANCE, "A"));
			}
		});

		executor.submit(new Runnable() {
			@Override
			public void run() {
				List<CompositeKey> keys = Arrays.asList(new CompositeKey("A", 1), new CompositeKey("A", 11));
				storage.getCache("a-A").invokeAll(keys, new ConditionalPut(AlwaysFilter.INSTANCE, "A"));
			}
		});
		
		Thread.sleep(1000000);
	}
	*/
	
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
