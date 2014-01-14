package org.gridkit.coherence.chtest.examples;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.gridkit.coherence.chtest.CacheConfig;
import org.gridkit.coherence.chtest.CacheConfig.ReplicatedScheme;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class ReplicatedTestLockBenchmark {

	private static final String LOCK_CACHE = "lock-cache";
	private static final String KEY_SET = "key.set";
	
	
	
	@Rule
	public DisposableCohCloud cloud = new DisposableCohCloud();
	
	@Before
	public void init() {
		
		CohNode all = cloud.all();
		all.presetFastLocalCluster();
		all.outOfProcess(true);
		all.logLevel(2);
		
		ReplicatedScheme cacheScheme = CacheConfig.replicatedScheme();
		cacheScheme.backingMapScheme(CacheConfig.localScheme());
		
		all.mapCache(LOCK_CACHE, cacheScheme);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void uniform__2_nodes() {
		
		List<String> keys = Arrays.asList("A", "B", "C", "D");
		
		long avg = testProfile(100000, keys, keys); 
		System.out.println("Testing 2 nodes with uniform key distribution");
		System.out.println(String.format("Average time %fms", (double)(avg) / TimeUnit.MILLISECONDS.toNanos(1)));
		
	}

	@Test
	@SuppressWarnings("unchecked")
	public void biased__2_nodes() {
		
		List<String> keys1 = Arrays.asList("A", "A", "A", "A", "A", "A", "A", "B", "C", "D");
		List<String> keys2 = Arrays.asList("B", "B", "B", "B", "B", "B", "B", "A", "C", "D");
		
		long avg = testProfile(100000, keys1, keys2); 
		System.out.println("Testing 2 nodes with biased key distribution");
		System.out.println(String.format("Average time %fms", (double)(avg) / TimeUnit.MILLISECONDS.toNanos(1)));
		
	}

	@Test
	@SuppressWarnings("unchecked")
	public void segragated__2_nodes() {
		
		List<String> keys1 = Arrays.asList("A1", "B1", "C1", "D1", "E1");
		List<String> keys2 = Arrays.asList("A2", "B2", "C2", "D2", "E2");
		
		long avg = testProfile(100000, keys1, keys2); 
		System.out.println("Testing 2 nodes with segragated key distribution");
		System.out.println(String.format("Average time %fms", (double)(avg) / TimeUnit.MILLISECONDS.toNanos(1)));
		
	}

	@Test
	@SuppressWarnings("unchecked")
	public void uniform__3_nodes() {
		
		List<String> keys = Arrays.asList("A", "B", "C", "D");
		
		long avg = testProfile(100000, keys, keys, keys); 
		System.out.println("Testing 3 nodes with uniform key distribution");
		System.out.println(String.format("Average time %fms", (double)(avg) / TimeUnit.MILLISECONDS.toNanos(1)));
		
	}

	@Test
	@SuppressWarnings("unchecked")
	public void biased__3_nodes() {
		
		List<String> keys1 = Arrays.asList("A", "A", "A", "A", "A", "A", "A", "B", "C", "D");
		List<String> keys2 = Arrays.asList("B", "B", "B", "B", "B", "B", "B", "A", "C", "D");
		List<String> keys3 = Arrays.asList("C", "C", "C", "C", "C", "C", "C", "A", "B", "D");
		
		long avg = testProfile(100000, keys1, keys2, keys3); 
		System.out.println("Testing 3 nodes with biased key distribution");
		System.out.println(String.format("Average time %fms", (double)(avg) / TimeUnit.MILLISECONDS.toNanos(1)));
		
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void segragated__3_nodes() {
		
		List<String> keys1 = Arrays.asList("A1", "B1", "C1", "D1", "E1");
		List<String> keys2 = Arrays.asList("A2", "B2", "C2", "D2", "E2");
		List<String> keys3 = Arrays.asList("A3", "B3", "C3", "D3", "E3");
		
		long avg = testProfile(100000, keys1, keys2, keys3); 
		System.out.println("Testing 3 nodes with segragated key distribution");
		System.out.println(String.format("Average time %fms", (double)(avg) / TimeUnit.MILLISECONDS.toNanos(1)));
		
	}	
	
	public long testProfile(final int iterations, List<String>... keySets) {
		for(int i = 0; i != keySets.length; ++i) {
			StringBuilder sb = new StringBuilder();
			for(String k: keySets[i]) {
				sb.append(' ').append(k);
			}
			cloud.node("node-" + i).setProp(KEY_SET, sb.toString());
		}
		
		cloud.all().getCache(LOCK_CACHE);
		
		List<Long> result = cloud.all().massExec(new Callable<Long>() {
			@Override
			public Long call() throws Exception {
				return test(iterations);
			}
		});
		
		long total = 0;
		for(Long t: result) {
			total += t;
		}
		
		long avg = total / (iterations * keySets.length);
		return avg;
	}
	
	public static long test(int iterations) {
		NamedCache cache = CacheFactory.getCache(LOCK_CACHE);
		String[] keys = System.getProperty(KEY_SET).trim().split("\\s");
		Random r = new Random();
		// quick warm up
		for(int i = 0; i != 200; ++i) {
			String key = keys[r.nextInt(keys.length)];
			cache.lock(key);
			cache.unlock(key);
		}
		long start = System.nanoTime();
		for(int i = 0; i != iterations; ++i) {
			String key = keys[r.nextInt(keys.length)];
			cache.lock(key);
			cache.unlock(key);
		}
		long time = System.nanoTime() - start;
		
		return time;
	}
}
