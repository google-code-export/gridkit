package org.gridkit.coherence.check;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.coherence.chtest.CohCloudRule;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.CacheFactory;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;

public class BMCheck {

	@Rule
	public CohCloudRule cloud = new DisposableCohCloud();
	
	private ExecutorService executor = Executors.newCachedThreadPool();

	@After
	public void shutdown_cluster_after_test() {
		executor.shutdownNow();
	}
	
	@Test
	public void touch_vanila_cache() throws InterruptedException, ExecutionException {

		cloud.all().presetFastLocalCluster();
		cloud.all().cacheConfig("bm-check-cache-config.xml");
		
		CohNode storage = cloud.node("storage");
		storage.localStorage(true);

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
		
		cloud.all().presetFastLocalCluster();
		cloud.all().cacheConfig("bm-check-cache-config.xml");
		
		CohNode storage = cloud.node("storage");
		storage.localStorage(true);
		
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
