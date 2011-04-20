package azul.test;

import static java.lang.System.getProperty;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

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
import azul.test.output.OutputWriter;
import azul.test.output.PoisonTask;
import azul.test.output.Task;
import azul.test.runner.BaseRunner;
import azul.test.runner.LimitedRunner;
import azul.test.runner.UnlimitedRunner;
import com.tangosol.net.CacheFactory;

public class Main {
	private static String mode = getProperty("mode") == null ? "heap" : getProperty("mode");
	
	private static String outputDir = getProperty("outputDir") == null ? "output" : getProperty("outputDir");
	
	private static int time = Integer.valueOf(getProperty("time") == null ? "180" : getProperty("time"));
	private static int warmUptime = Integer.valueOf(getProperty("warmUptime") == null ? "5" : getProperty("warmUptime"));
	private static int warmUpCount = Integer.valueOf(getProperty("warmUpCount") == null ? "1" : getProperty("warmUpCount"));
	
	private static String offHeapSize = getProperty("offHeapSize") == null ? "256" : getProperty("offHeapSize");
	private static int initCacheSize = Integer.valueOf(getProperty("initCacheSize") == null ? "25600" : getProperty("initCacheSize"));
	private static int maxCacheSize = Integer.valueOf(getProperty("maxCacheSize") == null ? "25600" : getProperty("maxCacheSize"));
	
	private static int recordSize = Integer.valueOf(getProperty("recordSize") == null ? "1024" : getProperty("recordSize"));
	private static int dispersion = Integer.valueOf(getProperty("dispersion") == null ? "256" : getProperty("dispersion"));
	
	private static int bulkSize = Integer.valueOf(getProperty("bulkSize") == null ? "1024" : getProperty("bulkSize"));

	private static boolean useSmartRecord = Boolean.valueOf(getProperty("useSmartRecord") == null ? "true" : getProperty("useSmartRecord"));
	
	private static int readersCount = Integer.valueOf(getProperty("readersCount") == null ? "5" : getProperty("readersCount"));
	private static int writersCount = Integer.valueOf(getProperty("writersCount") == null ? "1" : getProperty("writersCount"));
	
	private static float readersOps = Float.valueOf(getProperty("readersOps") == null ? "4" : getProperty("readersOps"));
	private static float writersOps = Float.valueOf(getProperty("writersOps") == null ? "7.2" : getProperty("writersOps"));
	
	private static int sampleSize = Integer.valueOf(getProperty("sampleSize") == null ? "1024" : getProperty("sampleSize"));
	private static int bufferSize = Integer.valueOf(getProperty("bufferSize") == null ? "2" : getProperty("bufferSize"));
	private static int loggersCount = Integer.valueOf(getProperty("loggersCount") == null ? "2" : getProperty("loggersCount"));
    private static boolean useEhcache = Boolean.valueOf(getProperty("useEhcache") == null ? "true" : getProperty("useEhcache"));
    private static boolean useCoherence = Boolean.valueOf(getProperty("useCoherence") == null ? "false" : getProperty("useCoherence"));
	
	private static Map<String, String> overallResults = new ConcurrentHashMap<String, String>();
	
	private static CacheManager manager;
	private static Cache cache;
    private static Map<Integer, Record> map;

	
	public static void main(String args[]) throws InterruptedException, ExecutionException, IOException {
		System.setProperty("org.terracotta.license.path", "terracotta-license.key");
        if(useEhcache)
            manager = new CacheManager("ehcache.xml");
        
        if(useEhcache)
            createCache();
        else if(useCoherence)
            createNamedCache();
        else
            createMap();
        
        System.out.println("Filling cache ...");
        if(useEhcache)
            fillCache();
        else
            fillMap();
        
        for (int i = 0; i < warmUpCount; ++i) {
        	System.out.println("Warming Up " + i + " ...");
        	runTest(false, warmUptime);
        }
        
        System.out.println("Running ... ");
        runTest(true, time);
        System.out.println("Completed");
        
        System.out.println("Files written: " + OutputWriter.filesClosed.get());
	}
	
	public static void runTest(boolean isRealRun, int time) throws InterruptedException, ExecutionException, IOException {
		if (isRealRun)
			(new File(outputDir)).mkdirs();
		
		LinkedBlockingQueue<Task> logQueue = new LinkedBlockingQueue<Task>();
		
		ExecutorService logThreadPool = Executors.newFixedThreadPool(loggersCount);
		
		for (int i=0; i < loggersCount; ++i)
			logThreadPool.submit(new OutputWriter(logQueue));
		
		ExecutorService serveThreadPool = Executors.newSingleThreadExecutor();
		ObservationLogger gcLogger = isRealRun ? new OutputObservationLogger(outputDir + "/gc.txt", logQueue, sampleSize, bufferSize) : new DummyObservationLogger();
		serveThreadPool.submit(new GCLogger(gcLogger));
		
		ExecutorService mainThreadPool = Executors.newFixedThreadPool(readersCount + writersCount);
		
		List<BaseRunner> runners = new ArrayList<BaseRunner>();

		for (int i=0; i < readersCount; ++i) {
			ObservationLogger logger = new DummyObservationLogger();
			
			if (isRealRun)
				logger = new OutputObservationLogger(outputDir + "/reader" + i + ".txt", logQueue, sampleSize, bufferSize);
			
		    Runnable reader;

            if(useEhcache)
                reader = new Reader(cache, maxCacheSize, bulkSize);
            else
                reader = new MapReader(map, maxCacheSize, bulkSize);
			
			if (readersOps > 0)
				runners.add(new LimitedRunner(reader, time, readersOps, logger));
			else
				runners.add(new UnlimitedRunner(reader, time, logger));
		}
		
		for (int i=0; i < writersCount; ++i) {
			ObservationLogger logger = new DummyObservationLogger();
			
			if (isRealRun)
				logger = new OutputObservationLogger(outputDir + "/writer" + i + ".txt", logQueue, sampleSize, bufferSize);
			
			Runnable writer;

            if(useEhcache)
                writer = new Writer(cache, maxCacheSize, recordSize, dispersion, useSmartRecord, bulkSize);
            //else if(useCoherence)
            //    writer = new BatchWriter(map, maxCacheSize, recordSize, dispersion, useSmartRecord, bulkSize);
            else
                writer = new MapWriter(map, maxCacheSize, recordSize, dispersion, useSmartRecord, bulkSize);
			
			if (writersOps > 0)
				runners.add(new LimitedRunner(writer, time, writersOps, logger));
			else
				runners.add(new UnlimitedRunner(writer, time, logger));
		}
		
		System.gc();
		
		long t = System.currentTimeMillis();
		
		mainThreadPool.invokeAll(runners);
		
		overallResults.put("workTime", (System.currentTimeMillis() - t)/1000.0 + "");
		
		mainThreadPool.shutdown();
		mainThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		
		serveThreadPool.shutdownNow();
		serveThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		
		for (int i=0; i < loggersCount; ++i)
			logQueue.add(new PoisonTask());
		
		logThreadPool.shutdown();
		logThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		
		if (isRealRun)
			printOverallResults();
	}
	
	public static void createCache() {
		if ("offHeap".equalsIgnoreCase(mode)) createOffHeapCache(); else createHeapCache();
	}
    
    public static void createMap(){
        System.out.println("Using ConcurrentHashMap.");
        map = new ConcurrentHashMap<Integer, Record>(initCacheSize);
    }
    
    @SuppressWarnings("unchecked")
    public static void createNamedCache(){
        System.out.println("Using Coherence.");
        System.setProperty("tangosol.coherence.cacheconfig", "coherence-cache-config.xml");
        System.setProperty("tangosol.coherence.distributed.localstorage", "false");
        map = CacheFactory.getCache("test");

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

    public static void fillMap() {
		map.clear();

		Random rand = new Random(System.currentTimeMillis());

		System.gc();
		long initFreeMemory = Runtime.getRuntime().freeMemory();

		List<Integer> allKeys = new ArrayList<Integer>(maxCacheSize);
        Map<Integer, Record> tmp = new HashMap<Integer, Record>(10000);
		for (int i = 0; i < maxCacheSize; ++i)
			allKeys.add(i);
		Collections.shuffle(allKeys, rand);

		for (int i = 0; i < initCacheSize; ++i) {
	    	if (!useSmartRecord)
	    		tmp.put(allKeys.get(i), new Record(rand, recordSize, dispersion));
	    	else
	    		tmp.put(allKeys.get(i), new SmartRecord(rand, recordSize, dispersion));
            if(i % 10000 == 0){
                map.putAll(tmp);
                tmp.clear();
            }
		}
        map.putAll(tmp);
        tmp.clear();

		System.gc();
		overallResults.put("cacheSize", map.size() + "");
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

