package org.gridkit.coherence.check;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.gridkit.coherence.chtest.CohHelper;
import org.gridkit.utils.vicluster.ViCluster;
import org.gridkit.utils.vicluster.ViNode;
import org.junit.After;
import org.junit.Test;

import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.CacheFactory;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;

public class BMCheck {

	
	private ViCluster cluster;
	private ExecutorService executor = Executors.newCachedThreadPool();

	@After
	public void shutdown_cluster_after_test() {
		cluster.shutdown();
		executor.shutdownNow();
	}
	
	@Test
	public void touch_vanila_cache() throws InterruptedException, ExecutionException {
		
		cluster = new ViCluster("test", "org.gridkit", "com.tangosol");
		
		CohHelper.cacheConfig(cluster, "bm-check-cache-config.xml");
		
		ViNode storage = cluster.node("storage");
		CohHelper.localstorage(storage, true);

		storage.exec(new Runnable() {
			
			@Override
			public void run() {
				CacheFactory.getCache("vanila-a").addMapListener(new Listener2());
			}
		});
		storage.getCache("vanila-a").put("A", "A");
	}

	@Test
	public void touch_partitioned_cache() throws InterruptedException, ExecutionException {
		
		cluster = new ViCluster("test", "org.gridkit", "com.tangosol");
		
		CohHelper.cacheConfig(cluster, "bm-check-cache-config.xml");
		
		ViNode storage = cluster.node("storage");
		CohHelper.localstorage(storage, true);
		
		storage.getCache("partitioned-a").put("A", "A");
	}
	
	public static class Listener implements MapListener {

		public Listener(BackingMapManagerContext ctx, String name) {
			System.out.println(ctx);
			System.out.println(name);
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
	public static class Listener2 implements MapListener {
		
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
