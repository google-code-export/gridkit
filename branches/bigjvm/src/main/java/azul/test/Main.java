package azul.test;

import static java.lang.System.getProperty;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import azul.test.output.DummyObservationLogger;
import azul.test.output.GCLogger;
import azul.test.output.ObservationLogger;
import azul.test.output.OutputObservationLogger;
import azul.test.output.OutputTask;
import azul.test.output.OutputWriter;
import azul.test.runner.BaseRunner;
import azul.test.runner.LimitedRunner;
import azul.test.runner.UnlimitedRunner;
import azul.test.util.ArrayUtil;

public class Main {
	private static String mode = getProperty("mode") == null ? "heap" : getProperty("mode");
	
	private static String outputDir = getProperty("outputDir") == null ? "output" : getProperty("outputDir");
	
	private static int time = Integer.valueOf(getProperty("time") == null ? "5" : getProperty("time"));
	private static int warmUptime = Integer.valueOf(getProperty("warmUptime") == null ? "2" : getProperty("warmUptime"));
	private static int warmUpCount = Integer.valueOf(getProperty("warmUpCount") == null ? "2" : getProperty("warmUpCount"));
	
	private static String offHeapSize = getProperty("offHeapSize") == null ? "256" : getProperty("sampleSize");
	private static int initCacheSize = Integer.valueOf(getProperty("initCacheSize") == null ? "100000" : getProperty("initCacheSize"));
	private static int maxCacheSize = Integer.valueOf(getProperty("maxCacheSize") == null ? "100000" : getProperty("maxCacheSize"));
	
	private static int arraySize = Integer.valueOf(getProperty("arraySize") == null ? "1024" : getProperty("arraySize"));
	private static int dispersion = Integer.valueOf(getProperty("dispersion") == null ? "256" : getProperty("dispersion"));
	
	private static int bulkSize = Integer.valueOf(getProperty("bulkSize") == null ? "1024" : getProperty("bulkSize"));
	
	private static int sampleSize = Integer.valueOf(getProperty("sampleSize") == null ? "1024" : getProperty("sampleSize"));
	private static int bufferSize = Integer.valueOf(getProperty("bufferSize") == null ? "2" : getProperty("bufferSize"));
	
	private static int loggersCount = Integer.valueOf(getProperty("loggersCount") == null ? "2" : getProperty("loggersCount"));
	
	private static int readersCount = Integer.valueOf(getProperty("readersCount") == null ? "8" : getProperty("readersCount"));
	private static int writersCount = Integer.valueOf(getProperty("writersCount") == null ? "8" : getProperty("writersCount"));
	
	private static int readersOps = Integer.valueOf(getProperty("readersOps") == null ? "0" : getProperty("readersOps"));
	private static int writersOps = Integer.valueOf(getProperty("writersOps") == null ? "50" : getProperty("writersOps"));
	
	private static Map<String, String> overallResults = new ConcurrentHashMap<String, String>();
	
	private static CacheManager manager;
	private static Cache cache;
	
	public static void main(String args[]) throws InterruptedException, ExecutionException, IOException {
		System.setProperty("org.terracotta.license.path", "terracotta-license.key");
        manager = new CacheManager("ehcache.xml");
        
        createCache();
        
        fillCache();
        
        for (int i = 0; i < warmUpCount; ++i) {
        	System.out.println("Warming Up " + i + " ...");
        	runTest(false, warmUptime);
        }
        
        System.out.println("Running ... ");
        runTest(true, time);
        System.out.println("Completed");
        
        System.exit(Reader.sum);
	}
	
	public static void runTest(boolean isRealRun, int time) throws InterruptedException, ExecutionException, IOException {
		System.gc();
		
		if (isRealRun)
			(new File(outputDir)).mkdirs();
		
		LinkedBlockingQueue<OutputTask> logQueue = new LinkedBlockingQueue<OutputTask>();
		
		ExecutorService serveThreadPool = Executors.newFixedThreadPool(loggersCount + 1);
		
		for (int i=0; i < loggersCount; ++i)
			serveThreadPool.submit(new OutputWriter(logQueue));
		
		ObservationLogger gcLogger = isRealRun ? new OutputObservationLogger(outputDir + "/gc.txt", logQueue, sampleSize, bufferSize) : new DummyObservationLogger();
		Future<Void> gcTask = serveThreadPool.submit(new GCLogger(gcLogger));
		
		ExecutorService mainThreadPool = Executors.newFixedThreadPool(readersCount + writersCount);
		
		List<BaseRunner> runners = new ArrayList<BaseRunner>();

		for (int i=0; i < readersCount; ++i) {
			ObservationLogger logger = new DummyObservationLogger();
			
			if (isRealRun)
				logger = new OutputObservationLogger(outputDir + "/reader" + i + ".txt", logQueue, sampleSize, bufferSize);
			
				Reader reader = new Reader(cache, maxCacheSize, bulkSize);
			
			if (readersOps > 0)
				runners.add(new LimitedRunner(reader, time, readersOps, logger));
			else
				runners.add(new UnlimitedRunner(reader, time, logger));
		}
		
		for (int i=0; i < writersCount; ++i) {
			ObservationLogger logger = new DummyObservationLogger();
			
			if (isRealRun)
				logger = new OutputObservationLogger(outputDir + "/writer" + i + ".txt", logQueue, sampleSize, bufferSize);
			
			Writer writer = new Writer(cache, maxCacheSize, arraySize, dispersion, bulkSize);
			
			if (writersOps > 0)
				runners.add(new LimitedRunner(writer, time, writersOps, logger));
			else
				runners.add(new UnlimitedRunner(writer, time, logger));
		}
		
		long t = System.currentTimeMillis();
		
		mainThreadPool.invokeAll(runners);
		
		overallResults.put("workTime", (System.currentTimeMillis() - t)/1000.0 + "");
		
		mainThreadPool.shutdown();
		
		gcTask.cancel(true);
		
		while (!gcTask.isDone() && !logQueue.isEmpty());
		
		serveThreadPool.shutdown();
		
		if (isRealRun)
			printOverallResults();
	}
	
	public static void createCache() {
		if ("offHeap".equalsIgnoreCase(mode)) createOffHeapCache(); else createHeapCache();
	}
	
	public static Cache createHeapCache() {
		CacheConfiguration config = new CacheConfiguration("heap", maxCacheSize);
		
		cache = new Cache(config);
		
		manager.addCache(cache);
		
		return cache;
	}
	
	public static Cache createOffHeapCache() {
		CacheConfiguration config = new CacheConfiguration("offHeap", 100);
		
		config.setOverflowToOffHeap(true);
		
		config.setMaxMemoryOffHeap(offHeapSize + "M");
		
		cache = new Cache(config);
		
		manager.addCache(cache);
		
		return cache;
	}
	
	public static void fillCache() {
		cache.removeAll();
		
		Random rand = new Random(System.currentTimeMillis());
		
		System.gc();
		long initFreeMemory = Runtime.getRuntime().freeMemory();
		
		while (cache.getSize() < initCacheSize)
			cache.put(new Element(rand.nextInt(maxCacheSize), ArrayUtil.createRandomArray(rand, arraySize, dispersion)));
		
		System.gc();
		overallResults.put("heapSizeForCache", (initFreeMemory - Runtime.getRuntime().freeMemory()) / (1024.0 * 1024) + "");
	}
	
	public static void printOverallResults() throws IOException {
		java.io.FileWriter writer = new java.io.FileWriter(outputDir + "/overall.txt");
		
		for (Map.Entry<String, String> entry : overallResults.entrySet())
			writer.write(entry.getKey() + " = " + entry.getValue() + "\n");
		
		writer.flush();
		writer.close();
	}
}

