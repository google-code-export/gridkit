package azul.test;

import static java.lang.System.getProperty;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import azul.test.data.Record;
import azul.test.data.SmartRecord;
import azul.test.output.DummyObservationLogger;
import azul.test.output.GCLogger;
import azul.test.output.ObservationLogger;
import azul.test.output.OutputObservationLogger;
import azul.test.output.OutputTask;
import azul.test.output.OutputWriter;
import azul.test.runner.BaseRunner;
import azul.test.runner.LimitedRunner;
import azul.test.runner.UnlimitedRunner;

public class Main {
	private static String mode = getProperty("mode") == null ? "heap" : getProperty("mode");
	
	private static String outputDir = getProperty("outputDir") == null ? "output" : getProperty("outputDir");
	
	private static int time = Integer.valueOf(getProperty("time") == null ? "2" : getProperty("time"));
	private static int warmUptime = Integer.valueOf(getProperty("warmUptime") == null ? "2" : getProperty("warmUptime"));
	private static int warmUpCount = Integer.valueOf(getProperty("warmUpCount") == null ? "2" : getProperty("warmUpCount"));
	
	private static String offHeapSize = getProperty("offHeapSize") == null ? "256" : getProperty("offHeapSize");
	private static int initCacheSize = Integer.valueOf(getProperty("initCacheSize") == null ? "1000" : getProperty("initCacheSize"));
	private static int maxCacheSize = Integer.valueOf(getProperty("maxCacheSize") == null ? "1000" : getProperty("maxCacheSize"));
	
	private static int recordSize = Integer.valueOf(getProperty("recordSize") == null ? "1024" : getProperty("recordSize"));
	private static int dispersion = Integer.valueOf(getProperty("dispersion") == null ? "256" : getProperty("dispersion"));
	
	private static int bulkSize = Integer.valueOf(getProperty("bulkSize") == null ? "1024" : getProperty("bulkSize"));

	private static boolean useSmartRecord = Boolean.valueOf(getProperty("useSmartRecord") == null ? "true" : getProperty("useSmartRecord"));
	
	private static int readersCount = Integer.valueOf(getProperty("readersCount") == null ? "20" : getProperty("readersCount"));
	private static int writersCount = Integer.valueOf(getProperty("writersCount") == null ? "0" : getProperty("writersCount"));
	
	private static int readersOps = Integer.valueOf(getProperty("readersOps") == null ? "0" : getProperty("readersOps"));
	private static int writersOps = Integer.valueOf(getProperty("writersOps") == null ? "0" : getProperty("writersOps"));
	
	private static int sampleSize = Integer.valueOf(getProperty("sampleSize") == null ? "1024" : getProperty("sampleSize"));
	private static int bufferSize = Integer.valueOf(getProperty("bufferSize") == null ? "2" : getProperty("bufferSize"));
	private static int loggersCount = Integer.valueOf(getProperty("loggersCount") == null ? "1" : getProperty("loggersCount"));
	
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
        
        System.out.println("files written: " + OutputWriter.filesClosed.get());
        System.out.println("workers closed " + UnlimitedRunner.closed.get());
        System.out.println("gc closed " + GCLogger.closed.get());
	}
	
	public static void runTest(boolean isRealRun, int time) throws InterruptedException, ExecutionException, IOException {
		UnlimitedRunner.closed.set(0);
		GCLogger.closed.set(0);
		
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
			
			Writer writer = new Writer(cache, maxCacheSize, recordSize, dispersion, useSmartRecord, bulkSize);
			
			if (writersOps > 0)
				runners.add(new LimitedRunner(writer, time, writersOps, logger));
			else
				runners.add(new UnlimitedRunner(writer, time, logger));
		}
		
		long t = System.currentTimeMillis();
		
		mainThreadPool.invokeAll(runners);
		
		overallResults.put("workTime", (System.currentTimeMillis() - t)/1000.0 + "");
		
		mainThreadPool.shutdownNow();
		mainThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		
		gcTask.cancel(true);
		try {gcTask.get();} catch (Exception e) {}
		
        //Thread.sleep(10000);
		while (!logQueue.isEmpty());
		
		serveThreadPool.shutdownNow();
		serveThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		
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
		
		
		System.out.println("-------- Allocating " + offHeapSize + "M for off heap");
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
		
		List<Integer> allKeys = new ArrayList<Integer>(maxCacheSize);
		for (int i = 0; i < maxCacheSize; ++i)
			allKeys.add(i);
		Collections.shuffle(allKeys, rand);
		
		for (int i = 0; i < initCacheSize; ++i) {
	    	if (!useSmartRecord)
	    		cache.put(new Element(allKeys.get(i), new Record(rand, recordSize, dispersion)));
	    	else
	    		cache.put(new Element(allKeys.get(i), new SmartRecord(rand, recordSize, dispersion)));
		}
		
		System.gc();
		overallResults.put("cacheSize", cache.getSize() + "");
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

