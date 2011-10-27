package org.gridkit.litter.processing;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import org.gridkit.monitoring.cpureport.CpuUsageReporter;

import com.tangosol.net.CacheFactory;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@SuppressWarnings("unchecked")
public class CohMiniTest {
	
	static void println() {
	    System.out.println();
	}
	
	static long start = System.nanoTime();
	
	static void println(String text) {
		long millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
	    System.out.println(String.format("[%1$tH:%1$tM:%1$tS.%1$tL/%2$d.%3$03d] ", new Date(), millis / 1000, millis % 1000)+ text);
	}	

//	private static BinaryStore CACHE = new InHeapBinaryStoreManager2("test", 4 << 20, 576 << 20).create();
//	private static Map<Object, Object> CACHE;
//	static {
//		try {
////			CACHE = (Map<Object, Object>) Class.forName(System.getProperty("litter.test-map", "java.util.HashMap")).newInstance();
//			CACHE = (Map<Object, Object>) Class.forName(System.getProperty("litter.test-map", "org.gridkit.litter.processing.PagedBinaryPackedMap")).newInstance();
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
//	}
	private static int OBJECT_SIZE = Integer.getInteger("litter.object-size", 16);
	private static int OBJECT_SIZE_DEVIATION = Integer.getInteger("litter.object-size-dev", 10);
	private static int NUMBER_OF_OBJECTS = Integer.getInteger("litter.number-of-objects", 1000000);
//	private static int NUMBER_OF_OBJECTS = Integer.getInteger("litter.number-of-objects", 2000000);
//	private static int NUMBER_OF_OBJECTS = Integer.getInteger("litter.number-of-objects", 8000000);
	private static int NUMBER_OF_HOLES = Integer.getInteger("litter.number-of-holes", NUMBER_OF_OBJECTS / 4);
	private static int REPORTING_INTERVAL = Integer.getInteger("litter.reporting-interval", 100000);
	private static int SLOW_OP_THRESHOLD = Integer.getInteger("litter.slow-op-threshold", 50);

	private static int POPULATE_THREAD_COUNT = Integer.getInteger("litter.populate.threads", 2);
	private static int TEST_THREAD_COUNT = Integer.getInteger("litter.test.threads", 2);
	
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		
		CpuUsageReporter.startReporter();
		
		if (System.getProperty("benchmark-default-scheme") == null) {
			System.setProperty("benchmark-default-scheme", "simple-distributed-scheme");
		}
		System.setProperty("tangosol.pof.enabled", "true");
		System.setProperty("tangosol.coherence.cacheconfig", "capacity-benchmark-cache-config.xml");

		if (args.length > 0 && "client".equals(args[0])) {
			// client mode
			System.setProperty("tangosol.coherence.distributed.localstorage", "false");
			
			println("Client mode");
			CohMiniTest cohMiniTest = new CohMiniTest();
			cohMiniTest.start();
		}
		else if (args.length > 0 && "server".equals(args[0])) {
			// server mode
			System.setProperty("tangosol.coherence.distributed.localstorage", "true");
			
			CacheFactory.getCache("objects");
			println("Cache node has started");
			
			while(true) {
				LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(300));
			}
		}
		else {
			// standalone mode
		    System.setProperty("tangosol.coherence.distributed.localstorage", "true");
		    
		    println("Standalone mode");
			CohMiniTest cohMiniTest = new CohMiniTest();
			cohMiniTest.start();
		}
	}
	
	private AtomicLong size = new AtomicLong();
	private AtomicLong tickCounter = new AtomicLong();
	private long startTime;
	
	private Map store;
	
	public void start() throws InterruptedException, ExecutionException {
		
		System.out.println("Object size: " + OBJECT_SIZE + "(-/+" + OBJECT_SIZE_DEVIATION + ")");		
		System.out.println("Number of objects: " + NUMBER_OF_OBJECTS + "(+ " + NUMBER_OF_HOLES + " holes)");		
		System.out.println("Loading by " + POPULATE_THREAD_COUNT + " thread(s)");		
		System.out.println("Testing by " + TEST_THREAD_COUNT + " thread(s)");		
	
		store = CacheFactory.getCache("objects");
		
//		PauseDetector.activate();
				
		Random rnd = new Random(1);
		
		tickCounter.set(0);
		startTime = System.nanoTime();
		size.set(0);
		ExecutorService threadPool = Executors.newFixedThreadPool(Math.max(POPULATE_THREAD_COUNT, TEST_THREAD_COUNT));
		Future<?>[] populateFeatures = new Future[POPULATE_THREAD_COUNT];
		for(int i = 0; i != POPULATE_THREAD_COUNT; ++i) {
			populateFeatures[i] = threadPool.submit(new Runnable() {
				@Override
				public void run() {
					populate(new Random());
				}
			});
		}

		for(int i = 0; i != POPULATE_THREAD_COUNT; ++i) {
			populateFeatures[i].get();
		}

		System.out.println("Initial loading complete");
		System.out.println("Real size: " + store.size());
		System.out.println("Loading time: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) / 1000d + " (sec)");
//		System.gc();
		
		tickCounter.set(0);
		startTime = System.nanoTime();
		for(int i = 0; i != TEST_THREAD_COUNT; ++i) {
			threadPool.execute(new Runnable() {
				@Override
				public void run() {
					while(true) {
						loop(new Random());
					}		
				}
			});
		}
	}

	private void populate(Random rnd) {
		Map<String, String> buffer = new HashMap<String, String>();
		while(store.size() < NUMBER_OF_OBJECTS) {
			
			long ss = System.nanoTime();
			
			while(buffer.size() < 100) { 
				String key = randomKey(rnd);
				String value = randomString(rnd);
				buffer.put(key, value);
			}
			
			store.putAll(buffer);
			buffer.clear();
			
			long st = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - ss);
			if (st > SLOW_OP_THRESHOLD ) {
				println("Slow operation (population): " + st + "ms");
			}

//			if (cs % 100000 == 0) {
//				println("Done " + cs);
//			}
			
			long nt = tickCounter.incrementAndGet();
			
			if (nt % (REPORTING_INTERVAL / 100) == 0) {
				long time = System.nanoTime() - startTime;
				println("Population iteration " + tickCounter);
				double pace = (double)REPORTING_INTERVAL / time * TimeUnit.SECONDS.toNanos(1);
				println("Population 'put' speed: " + pace + " (" + POPULATE_THREAD_COUNT + " threads)");
				long memUsage = Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory();
				println("Cache size: " + store.size() + " (" + NUMBER_OF_OBJECTS + ")");				
				println("Mem usage: " + (memUsage >> 10) + "k");				
				startTime = System.nanoTime();
			}
		}
	}

	private Object storeGet(String key) {
		return store.get(key);
	}

	private void storePut(String key, String value) {
		store.put(key, value);
	}

	private void storeRemove(String key) {
		store.remove(key);
	}

	private String randomKey(Random rnd) {
		long key = 100000 + rnd.nextInt(NUMBER_OF_OBJECTS + NUMBER_OF_HOLES);
		return String.valueOf(key);
	}

	private void loop(Random rnd) {
		while(true) {
			long ss = System.nanoTime();
			
			long cs = size.get();
			if (cs > NUMBER_OF_OBJECTS) {
				if ((cs - NUMBER_OF_OBJECTS) >= rnd.nextInt(NUMBER_OF_HOLES)) {
					String key = randomKey(rnd);
					if (storeGet(key) != null) {
						size.decrementAndGet();
						storeRemove(key);
						continue;
					}
				}
			}
	
			String key = randomKey(rnd);
			if (storeGet(key) == null) {
				size.incrementAndGet();
			}
			storePut(key, randomString(rnd));
			
			long nt = tickCounter.incrementAndGet();
			
			long st = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - ss);
			if (st > SLOW_OP_THRESHOLD ) {
				println("Slow operation: " + st + "ms");
			}
			
			if (nt % REPORTING_INTERVAL == 0) {
				long time = System.nanoTime() - startTime;
				println("Iteration " + tickCounter);
				double pace = (double)REPORTING_INTERVAL / time * TimeUnit.SECONDS.toNanos(1);
				println("Tick speed: " + pace + " (" + TEST_THREAD_COUNT + " threads)");
				long memUsage = Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory();
				println("Mem usage: " + (memUsage >> 10) + "k");				
				startTime = System.nanoTime();
				break;
			}
		}
	}

	private String randomString(Random rnd) {
		int size = OBJECT_SIZE - OBJECT_SIZE_DEVIATION + rnd.nextInt(2 * OBJECT_SIZE_DEVIATION);
		String str = randomString(size, rnd);
		return str;
	}
	
	public String randomString(int len, Random rnd) {
		char[] buffer = new char[len];
		for(int i = 0; i != len; ++i) {
			buffer[i] = (char)('A' + rnd.nextInt(23));
		}
		return new String(buffer, 0, len);
	}	
}
