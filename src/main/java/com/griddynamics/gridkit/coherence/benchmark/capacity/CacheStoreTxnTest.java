package com.griddynamics.gridkit.coherence.benchmark.capacity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;

public class CacheStoreTxnTest {

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
        System.setProperty("tangosol.coherence.distributed.localstorage", "true");
        
        System.setProperty("benchmark-default-scheme", "read-through-transactional-scheme");
        
        try {
            final NamedCache cache = CacheFactory.getCache("objects");
            
            Object[] keys = {"A", "B", "C"};            
            getKeys(cache, Arrays.asList(keys));
            
            List list = new ArrayList();
            for(int i = 0; i != 100; ++i) {
                list.add(String.valueOf(i));
            }
            
            getKeys(cache, list);            
            
        } catch (Exception e) {
            e.printStackTrace();
        }       
    }

    private static void getKeys(NamedCache cache, Collection keyList) {
        System.out.println("getKeys(" + keyList + ")");
        Map result = cache.getAll(keyList);
        System.out.println("result: " + new HashMap(result));        
    }

    private static void checkAccess(NamedCache cache, Filter filter) {

        cache.keySet(filter).size();
        
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        long start = System.nanoTime();
        int i;
        for(i = 1; i != 100; ++i) {
            cache.keySet(filter).size();
            if (System.nanoTime() - start > TimeUnit.SECONDS.toNanos(15)) {
                break;
            }
        }
        long finish = System.nanoTime();
      
        System.out.println("Filter time:" + (TimeUnit.NANOSECONDS.toMicros((finish - start) / i) / 1000d) + "(ms) - " + filter.toString());
    };
}
