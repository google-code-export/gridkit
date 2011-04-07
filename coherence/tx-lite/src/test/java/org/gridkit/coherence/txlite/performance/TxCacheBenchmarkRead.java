package org.gridkit.coherence.txlite.performance;

import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.gridkit.coherence.txlite.TxLite;

import sample.ObjectGenerator;
import sample.SimpleDomainObjGenerator;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.ExternalizableHelper;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class TxCacheBenchmarkRead {
	
	static void println() {
	    System.out.println();
	}
	
	static void println(String text) {
	    System.out.println(String.format("[%1$tH:%1$tM:%1$tS.%1$tL] ", new Date()) + text);
	}
	
	public static void main(String[] args) {
		new TxCacheBenchmarkRead().process();
	}
	
	public void process() {
	    
	    System.setProperty("tangosol.pof.enabled", "false");
//	    System.setProperty("tangosol.pof.config", "capacity-benchmark-pof-config.xml");
	    System.setProperty("tangosol.coherence.cacheconfig", "tx-lite-test-cache-config.xml");
	    System.setProperty("tangosol.coherence.distributed.localstorage", "false");
	    
	    
		try {
			final NamedCache cache = CacheFactory.getCache("t-objects");
			final ObjectGenerator<?, ?> generator = new SimpleDomainObjGenerator();
//			final ObjectGenerator<?, ?> generator = new SimpleDomainObjGenerator(100);
//			final ObjectGenerator<?, ?> generator = new SimpleDomainObjGenerator(400);
		
//			cache.addIndex(new ReflectionExtractor("getA0"), false, null);
//			cache.addIndex(new ReflectionExtractor("getAs"), false, null);			
			
//			System.out.println(CacheFactory.getClusterConfig().toString());
						
			
//			long objectCount = 1000000;
			long objectCount = 100000;
//			long objectCount = 200000;
//			long objectCount = 10000;
			
			long rangeStart = 1000000;
			long rangeFinish = 1000000 + objectCount;
			
			println("Cache size: " + cache.size());
			println("Loading " + objectCount + " objects ...");
			loadObjects(cache, generator, rangeStart, rangeFinish);			
			
			println("Loaded " + cache.size() + " objects");
			println("Key binary size: " + ExternalizableHelper.toBinary(generator.generate(1, 2).keySet().iterator().next()).length());
			println("Value binary size: " + ExternalizableHelper.toBinary(generator.generate(1, 2).values().iterator().next()).length());
			System.gc();
			println("Mem. usage " + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());

//			checkAccess(cache, new EqualsFilter("getA0", new DomainObjAttrib("?")));
//			checkAccess(cache, new EqualsFilter(new ReflectionPofExtractor("a0"), new DomainObjAttrib("?")));
//			checkAccess(cache, new EqualsFilter("getAs", Collections.EMPTY_LIST));
//			checkAccess(cache, new ContainsAnyFilter("getAs", Collections.singleton(new DomainObjAttrib("?"))));
			
//			ContinuousQueryCache view = new ContinuousQueryCache(cache, new EqualsFilter("getHashSegment", 0), true);
//			System.out.println("View size " + view.size());
//			
//			view.addIndex(new ReflectionExtractor("getA0"), false, null);
//            checkAccess(view, new EqualsFilter("getA0", new DomainObjAttrib("?")));
//            checkAccess(view, new EqualsFilter("getA1", new DomainObjAttrib("?")));
			
			while(true) {
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	private void loadObjects(NamedCache cache, ObjectGenerator<?, ?> generator, long rangeStart, long rangeFinish) {
//		cache = TxLite.ensureWriteable(cache);
//		cache = TxLite.ensureWriteable(cache);
		int putSize = 100;
		long blockTs = System.nanoTime();
		long blockStart = rangeStart;
		for(long i = rangeStart;  i < rangeFinish; i += putSize) {
		    if (i % 10000 == 0) {
		        String stats = "";
		        if (i > blockStart) {
		            long blockSize = i - blockStart;
		            long blockTime = System.nanoTime() - blockTs;
		            double avg = (((double)blockSize) / blockTime) * TimeUnit.SECONDS.toNanos(1);
		            stats = " block " + blockSize + " in " + TimeUnit.NANOSECONDS.toMillis(blockTime) + "ms, AVG: " + avg + " put/sec, " + avg/putSize + " tx/sec, batchSize " + putSize;
		        }
		        println("Done " + (i - rangeStart) + stats);
		        blockTs = System.nanoTime();
		        blockStart = i;
		    }
		    long j = Math.min(rangeFinish, i + putSize);
		    cache.getAll(new HashSet(generator.generate(i, j).keySet()));
		    TxLite.commit(cache);
		}
//		TxLite.commit(cache);
		TxLite.closeSession(cache);
	}

//    private static void checkAccess(NamedCache cache, Filter filter) {
//
//        cache.keySet(filter).size();
//        
//        try {
//            Thread.sleep(50);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        
//        long start = System.nanoTime();
//        int i;
//        for(i = 1; i != 100; ++i) {
//            cache.keySet(filter).size();
//            if (System.nanoTime() - start > TimeUnit.SECONDS.toNanos(15)) {
//                break;
//            }
//        }
//        long finish = System.nanoTime();
//      
//        System.out.println("Filter time:" + (TimeUnit.NANOSECONDS.toMicros((finish - start) / i) / 1000d) + "(ms) - " + filter.toString());
//    };
}
