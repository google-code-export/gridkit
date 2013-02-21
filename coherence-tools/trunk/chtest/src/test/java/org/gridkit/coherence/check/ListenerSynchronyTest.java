package org.gridkit.coherence.check;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.coherence.chtest.CohCloudRule;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.gridkit.coherence.test.CacheTemplate;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;
import com.tangosol.util.MapListenerSupport;

public class ListenerSynchronyTest {

	@Rule
	public CohCloudRule cloud = new DisposableCohCloud();
	private ExecutorService executor = Executors.newCachedThreadPool();

	@After
	public void shutdown_cluster_after_test() {
		executor.shutdownNow();
	}

	/**
	 * Synchronous listeners are expected to be invoked from service thread.
	 */	
	@Test
	public void check_synchronous_listener_semantic() throws InterruptedException, ExecutionException {

		cloud.all().presetFastLocalCluster();
		
		CacheTemplate.useTemplateCacheConfig(cloud.all());
		CacheTemplate.usePartitionedInMemoryCache(cloud.all());

		final CohNode client1 = cloud.node("client1");
		final CohNode client2 = cloud.node("client2");
		
		cloud.node("storage").localStorage(true);
		cloud.node("client*").localStorage(false);

		cloud.all().getCache("a-test");

		client1.exec(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				CacheFactory.getCache("a-test").addMapListener(new MapListenerSupport.SynchronousListener() {
					
					@Override
					public void entryUpdated(MapEvent e) {
						System.out.println("[" + Thread.currentThread().getName() + "]" + e.toString());
					}
					
					@Override
					public void entryInserted(MapEvent e) {
						System.out.println("[" + Thread.currentThread().getName() + "]" + e.toString());
						
					}
					
					@Override
					public void entryDeleted(MapEvent e) {
						System.out.println("[" + Thread.currentThread().getName() + "]" + e.toString());
					}
				});
				
				return null;
			}
		});
		
		Future<?> f = executor.submit(new Runnable() {
			
			@Override
			public void run() {
				System.out.println("Doing a put");
				client2.getCache("a-test").put("A", "A");
				
			}
		});
		
		f.get();
		
		Thread.sleep(100);
	}

	/**
	 * Normal listeners are expected to be invoked from event dispatch thread.
	 */
	@Test
	public void check_asynchronous_listener_semantic() throws InterruptedException, ExecutionException {

		cloud.all().presetFastLocalCluster();
		
		CacheTemplate.useTemplateCacheConfig(cloud.all());
		CacheTemplate.usePartitionedInMemoryCache(cloud.all());

		final CohNode client1 = cloud.node("client1");
		final CohNode client2 = cloud.node("client2");
		
		cloud.node("storage").localStorage(true);
		cloud.node("client*").localStorage(false);

		cloud.all().getCache("a-test");

		client1.exec(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				CacheFactory.getCache("a-test").addMapListener(new MapListener() {
					
					@Override
					public void entryUpdated(MapEvent e) {
						System.out.println("[" + Thread.currentThread().getName() + "]" + e.toString());
					}
					
					@Override
					public void entryInserted(MapEvent e) {
						System.out.println("[" + Thread.currentThread().getName() + "]" + e.toString());
						
					}
					
					@Override
					public void entryDeleted(MapEvent e) {
						System.out.println("[" + Thread.currentThread().getName() + "]" + e.toString());
					}
				});
				
				return null;
			}
		});
		
		Future<?> f = executor.submit(new Runnable() {
			
			@Override
			public void run() {
				System.out.println("Doing a put");
				client2.getCache("a-test").put("A", "A");
				
			}
		});
		
		f.get();

		Thread.sleep(100);
	}	
}
