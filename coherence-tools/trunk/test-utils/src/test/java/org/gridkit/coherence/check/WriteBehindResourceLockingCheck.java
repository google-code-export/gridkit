package org.gridkit.coherence.check;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.gridkit.coherence.test.CacheTemplate;
import org.gridkit.utils.vicluster.CohHelper;
import org.gridkit.utils.vicluster.ViCluster;
import org.gridkit.utils.vicluster.ViNode;
import org.hamcrest.CoreMatchers;
import org.hamcrest.number.IsGreaterThan;
import org.hamcrest.number.OrderingComparisons;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.RequestPolicyException;
import com.tangosol.net.cache.CacheStore;

public class WriteBehindResourceLockingCheck {

	private static final long CACHE_LOADER_DELAY = 2000;
	
	private ViCluster cluster;
	private ExecutorService executor = Executors.newCachedThreadPool();
	private ViNode client1;
	private ViNode client2;
	private ViNode client3;
	private ViNode client4;

	@After
	public void shutdown_cluster_after_test() {
		cluster.shutdown();
		executor.shutdownNow();
	}
	
	@Test
	public void verify_resource_lock_chaining() throws InterruptedException, ExecutionException {
		
		cluster = new ViCluster("test", "org.gridkit", "com.tangosol");
		
		CohHelper.enableFastLocalCluster(cluster);
		
		CacheTemplate.useTemplateCacheConfig(cluster);
		CacheTemplate.usePartitionedReadWriteCache(cluster, DelayCacheStore.class);
		CacheTemplate.usePartitionedServiceThreadCount(cluster, 10);
		CacheTemplate.useWriteDelay(cluster, "100s");

		ViNode storage = cluster.node("storage");
		CohHelper.localstorage(storage, true);

		storage.getCache("a-test");
		initClientsAndCache();

		delay(1000);
		
		verifyParallelReadThrough();
		verifyReadThroughContention();
	}

	@Test
	public void reproduce_guardian_timeout() throws InterruptedException, ExecutionException {
		
		cluster = new ViCluster("test", "org.gridkit", "com.tangosol");
		
		CohHelper.enableFastLocalCluster(cluster);
		
		CacheTemplate.useTemplateCacheConfig(cluster);
		CacheTemplate.usePartitionedReadWriteCache(cluster, DelayCacheStore.class);
		CacheTemplate.usePartitionedServiceThreadCount(cluster, 10);
		CacheTemplate.useWriteDelay(cluster, "100s");
		CacheTemplate.usePartitionedServiceGuardianTimeout(cluster, 2 * CACHE_LOADER_DELAY);

		ViNode storage = cluster.node("storage");
		CohHelper.localstorage(storage, true);

		storage.getCache("a-test");
		initClientsAndCache();

		delay(1000);
		
		verifyParallelReadThrough();
		
		try {
			verifyReadThroughContention();
			Assert.assertFalse("Should throw an exception", true);
		}
		catch(Exception e) {
			// We have only one storage node
			Assert.assertSame(RequestPolicyException.class, e.getCause().getCause().getClass());
		}
	}

	private void initClientsAndCache() {
		client1 = cluster.node("client1");
		client2 = cluster.node("client2");
		client3 = cluster.node("client3");
		client4 = cluster.node("client4");
		CohHelper.localstorage(client1, false);
		CohHelper.localstorage(client2, false);
		CohHelper.localstorage(client3, false);
		CohHelper.localstorage(client4, false);
		
		client1.getCache("a-test");
		client2.getCache("a-test");
		client3.getCache("a-test");
		client4.getCache("a-test");

		for(int i = 0; i != 23; ++i) {
			String key = String.valueOf((char)('A' + i));
			client1.getCache("a-test").put(key, key);
		}
	}
	
	private void verifyParallelReadThrough() {
		Future<Long> f1 = executor.submit(new Callable<Long>() {
			@Override
			public Long call() throws Exception {
				return measureGet(client1, "Z0-missing-1");
			}
		});
		Future<Long> f2 = executor.submit(new Callable<Long>() {
			@Override
			public Long call() throws Exception {
				return measureGet(client2, "Z0-missing-2");
			}
		});
		Future<Long> f3 = executor.submit(new Callable<Long>() {
			@Override
			public Long call() throws Exception {
				return measureGet(client3, "Z0-missing-3");
			}
		});
		Future<Long> f4 = executor.submit(new Callable<Long>() {
			@Override
			public Long call() throws Exception {
				return measureGet(client4, "Z0-missing-4");
			}
		});

		try {
			
			Assert.assertTrue(f1.get() + 2 >= CACHE_LOADER_DELAY);
			Assert.assertTrue(f1.get() + 2 <  3 * CACHE_LOADER_DELAY / 2);

			Assert.assertTrue(f2.get() + 2 >= CACHE_LOADER_DELAY);
			Assert.assertTrue(f2.get() + 2 <  3 * CACHE_LOADER_DELAY / 2);

			Assert.assertTrue(f3.get() + 2 >= CACHE_LOADER_DELAY);
			Assert.assertTrue(f3.get() + 2 <  3 * CACHE_LOADER_DELAY / 2);
			
			Assert.assertTrue(f4.get() + 2 >= CACHE_LOADER_DELAY);
			Assert.assertTrue(f4.get() + 2 <  3 * CACHE_LOADER_DELAY / 2);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void verifyReadThroughContention() {
		Future<Long> f1 = executor.submit(new Callable<Long>() {
			@Override
			public Long call() throws Exception {
				return measureGet(client1, "A", "B", "C", "D", "Z1-missing-1");
			}
		});
		Future<Long> f2 = executor.submit(new Callable<Long>() {
			@Override
			public Long call() throws Exception {
				return measureGet(client2, "B", "C", "D", "E", "Z1-missing-2");
			}
		});
		Future<Long> f3 = executor.submit(new Callable<Long>() {
			@Override
			public Long call() throws Exception {
				return measureGet(client3, "C", "D", "E", "F", "Z1-missing-3");
			}
		});
		Future<Long> f4 = executor.submit(new Callable<Long>() {
			@Override
			public Long call() throws Exception {
				return measureGet(client4, "D", "E", "F", "G", "Z1-missing-4");
			}
		});

		try {
			List<Long> timings = new ArrayList<Long>();

			timings.add(f1.get());
			timings.add(f2.get());
			timings.add(f3.get());
			timings.add(f4.get());
			
			Collections.sort(timings);
			
			Assert.assertThat(timings.get(0) + 2, OrderingComparisons.greaterThan(CACHE_LOADER_DELAY));
			Assert.assertThat(timings.get(1) + 2, OrderingComparisons.greaterThan(2 * CACHE_LOADER_DELAY));
			Assert.assertThat(timings.get(2) + 2, OrderingComparisons.greaterThan(3 * CACHE_LOADER_DELAY));
			Assert.assertThat(timings.get(3) + 2, OrderingComparisons.greaterThan(4 * CACHE_LOADER_DELAY));
			
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static long measureGet(ViNode node, final String... keys) {
			long start = System.nanoTime();
			node.exec(new Runnable() {
				@Override
				public void run() {
					CacheFactory.getCache("a-test").getAll(Arrays.asList(keys));
				}
			});
			long time2 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
			System.out.println("Get " + Arrays.toString(keys) + " in " + time2 + "ms");
			return time2;
		}

	public static long measurePutGet(ViNode node, final String... keys) {
		long start = System.nanoTime();
		node.exec(new Runnable() {
			@Override
			public void run() {
				for(String key: keys) {
					CacheFactory.getCache("a-test").put(key, key + "v2");
				}
			}
		});
		long time1 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
		
		start = System.nanoTime();
		System.out.println("Erase " + Arrays.toString(keys) + " in " + time1 + "ms");
		node.exec(new Runnable() {
			@Override
			public void run() {
				CacheFactory.getCache("a-test").getAll(Arrays.asList(keys));
			}
		});
		long time2 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
		System.out.println("Get " + Arrays.toString(keys) + " in " + time2 + "ms");
		return time2;
	}

	public static long measureEraseGet(ViNode node, final String... keys) {
		long start = System.nanoTime();
		node.exec(new Runnable() {
			@Override
			public void run() {
				for(String key: keys) {
					CacheFactory.getCache("a-test").remove(key);
				}
			}
		});
		long time1 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

		start = System.nanoTime();
		System.out.println("Erase " + Arrays.toString(keys) + " in " + time1 + "ms");
		node.exec(new Runnable() {
			@Override
			public void run() {
				CacheFactory.getCache("a-test").getAll(Arrays.asList(keys));
			}
		});
		long time2 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
		System.out.println("Get " + Arrays.toString(keys) + " in " + time2 + "ms");
		return time2;
	}

	public static void delay(long millis) {
		try {
			long deadLine = System.currentTimeMillis() + millis;
			while(true) {
				long sleepTime = deadLine - System.currentTimeMillis();
				if (sleepTime > 0) {
					Thread.sleep(sleepTime);
				}
				else {
					break;
				}
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static class DelayCacheStore implements CacheStore {

		static void println(String text) {
		    System.out.println(String.format("%1$tH:%1$tM:%1$tS.%1$tL ", new Date()) + text);
		}
		
		@Override
		public Object load(Object key) {
			println("DelayCacheStore::load <- " + key);
			delay(CACHE_LOADER_DELAY);
			println("DelayCacheStore::load -> ");
			return null;
		}

		@Override
		public Map loadAll(Collection keys) {
			println("DelayCacheStore::loadAll <- " + keys);
			delay(CACHE_LOADER_DELAY);
			println("DelayCacheStore::loadAll -> ");
			return Collections.EMPTY_MAP;
		}

		@Override
		public void erase(Object key) {
			println("DelayCacheStore::erase <- " + key);
			delay(CACHE_LOADER_DELAY);
			println("DelayCacheStore::erase -> ");
		}

		@Override
		public void eraseAll(Collection keys) {
			println("DelayCacheStore::eraseAll <- " + keys);
			delay(CACHE_LOADER_DELAY);
			println("DelayCacheStore::eraseAll -> ");
		}

		@Override
		public void store(Object key, Object value) {
			println("DelayCacheStore::store <- " + key + ", " + value);
			delay(CACHE_LOADER_DELAY);
			println("DelayCacheStore::store -> ");
		}

		@Override
		public void storeAll(Map entries) {
			println("DelayCacheStore::stoareAll <- " + entries.size() + " pairs, " +entries);
			delay(CACHE_LOADER_DELAY);
			println("DelayCacheStore::storeAll -> ");
		}
	}
}
