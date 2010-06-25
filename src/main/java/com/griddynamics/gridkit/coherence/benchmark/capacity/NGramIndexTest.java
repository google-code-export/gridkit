package com.griddynamics.gridkit.coherence.benchmark.capacity;

import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import sample.SimpleDomainObjGenerator;

import com.griddynamics.gridkit.coherence.benchmark.capacity.objects.ObjectGenerator;
import com.griddynamics.gridkit.coherence.index.ngram.ContainsSubstringFilter;
import com.griddynamics.gridkit.coherence.index.ngram.NGramExtractor;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;

public class NGramIndexTest {
    
    private static final String[] CHARSET1 = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O"};
    
    static void println() {
        System.out.println();
    }
    
    static void println(String text) {
        System.out.println(String.format("[%1$tH:%1$tM:%1$tS.%1$tL] ", new Date()) + text);
    }
    
    public static void main(String[] args) {
        
        System.setProperty("tangosol.pof.enabled", "true");
        System.setProperty("tangosol.pof.config", "capacity-benchmark-pof-config.xml");
        System.setProperty("tangosol.coherence.cacheconfig", "capacity-benchmark-cache-config.xml");
//      System.setProperty("benchmark-default-scheme", "local-scheme");     
//      System.setProperty("benchmark-default-scheme", "local-hashmap-scheme");
//      System.setProperty("benchmark-default-scheme", "local-juc-hashmap-scheme");
      System.setProperty("benchmark-default-scheme", "simple-distributed-scheme");
//      System.setProperty("benchmark-default-scheme", "external-distributed-scheme");
//      System.setProperty("benchmark-default-scheme", "partitioned-true-external-distributed-scheme");
//      System.setProperty("benchmark-default-scheme", "partitioned-false-external-distributed-scheme");
//      System.setProperty("benchmark-default-scheme", "simple-replicated-scheme");
//        System.setProperty("benchmark-default-scheme", "transactional-scheme");     
        
        try {
            final NamedCache cache = CacheFactory.getCache("objects");
            final ObjectGenerator<?, ?> generator = new SimpleDomainObjGenerator();
        
            NGramExtractor extractor = new NGramExtractor(3);
            cache.addIndex(extractor, false, null);
            
            long objectCount = 1 << 12;
//          long objectCount = 100000;
            
            long rangeStart = 0;
            long rangeFinish = rangeStart + objectCount;
            
            println("Loading " + objectCount + " objects ...");
            int putSize = 10;
            long blockTs = System.nanoTime();
            long blockStart = rangeStart;
            for(long i = rangeStart;  i < rangeFinish; i += putSize) {
                if (i % 1000 == 0) {
                    String stats = "";
                    if (i > blockStart) {
                        long blockSize = i - blockStart;
                        long blockTime = System.nanoTime() - blockTs;
                        double avg = (((double)blockSize) / blockTime) * TimeUnit.SECONDS.toNanos(1);
                        stats = " block " + blockSize + " in " + TimeUnit.NANOSECONDS.toMillis(blockTime) + "ms, avg " + avg + " put/sec, batchSize " + putSize;
                    }
                    println("Done " + (i - rangeStart) + stats);
                    blockTs = System.nanoTime();
                    blockStart = i;
                }
                long j = Math.min(rangeFinish, i + putSize);
                cache.putAll(generateEncodedNumber(i, j, CHARSET1));
            }           
            
            println("Loaded " + cache.size() + " objects");
            System.gc();
            println("Mem. usage " + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());

            println("Query substring [CDEFGHIJK]");
            checkAccess(cache, new ContainsSubstringFilter("CDEFGHIJK"));
            println("Query substring [ABCDE]");
            checkAccess(cache, new ContainsSubstringFilter("ABCDE"));
            println("Query substring [BCDE]");
            checkAccess(cache, new ContainsSubstringFilter("BCDE"));
            println("Query substring [CDEF]");
            checkAccess(cache, new ContainsSubstringFilter("BCDE"));
            println("Query substring [DEF]");
            checkAccess(cache, new ContainsSubstringFilter("DEF"));
            println("Query substring [EFG]");
            checkAccess(cache, new ContainsSubstringFilter("EFG"));
            println("Query substring [FG]");
            checkAccess(cache, new ContainsSubstringFilter("FG"));
            println("Query substring [B]");
            checkAccess(cache, new ContainsSubstringFilter("B"));
//          checkAccess(cache, new EqualsFilter("getAs", Collections.EMPTY_LIST));
//          checkAccess(cache, new ContainsAnyFilter("getAs", Collections.singleton(new DomainObjAttrib("?"))));
            
//          ContinuousQueryCache view = new ContinuousQueryCache(cache, new EqualsFilter("getHashSegment", 0), true);
//          System.out.println("View size " + view.size());
//          
//          view.addIndex(new ReflectionExtractor("getA0"), false, null);
//            checkAccess(view, new EqualsFilter("getA0", new DomainObjAttrib("?")));
//            checkAccess(view, new EqualsFilter("getA1", new DomainObjAttrib("?")));

            
            while(true) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }       
    }

    private static void checkAccess(NamedCache cache, Filter filter) {

        cache.keySet(filter).size();
        
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        long start = System.nanoTime();
        int rsSize = -1;
        int i;
        for(i = 1; i != 100; ++i) {
            rsSize = cache.keySet(filter).size();
            if (System.nanoTime() - start > TimeUnit.SECONDS.toNanos(15)) {
                break;
            }
        }
        long finish = System.nanoTime();
      
        System.out.println("Query result set: " + rsSize);
        System.out.println("Query time: " + (TimeUnit.NANOSECONDS.toMicros((finish - start) / i) / 1000d) + "(ms) - " + filter.toString());        
    };

    static Map<String, String> generateEncodedNumber(long from, long to, String[] charset) {
        Map<String, String> result = new HashMap<String, String>();
        for (int i = 0; i != to; ++i) {
            String text = encodeBitPositions(i, charset);
            result.put(text, text);
        }
        return result;
    }    
    
    static String encodeBitPositions(long value, String[] charset) {
        StringBuilder builder = new StringBuilder();
        for(int i =0 ; i != charset.length; ++i) {
            if ((value & (1 << i)) != 0) {
                builder.append(charset[i]);
            }
        }
        return builder.toString();
    }
}
