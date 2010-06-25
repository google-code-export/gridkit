package com.griddynamics.gridkit.coherence.benchmark.capacity;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import sample.SimpleDomainObjGenerator;

import com.griddynamics.gridkit.coherence.benchmark.capacity.objects.ObjectGenerator;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

public class DeadlockTest {
    static void println() {
        System.out.println();
    }
    
    static void println(String text) {
        System.out.println(String.format("[%1$tH:%1$tM:%1$tS.%1$tL] ", new Date()) + text);
    }
    
    public static void main(String[] args) {
        
        System.setProperty("tangosol.pof.enabled", "false");
//        System.setProperty("tangosol.pof.config", "capacity-benchmark-pof-config.xml");
        System.setProperty("tangosol.coherence.cacheconfig", "capacity-benchmark-cache-config.xml");
        System.setProperty("tangosol.coherence.distributed.localstorage", "true");
        
        System.setProperty("benchmark-default-scheme", "simple-distributed-scheme");
        
        try {
            final NamedCache cache1 = CacheFactory.getCache("objects1");
            final NamedCache cache2 = CacheFactory.getCache("objects2");
            
            println("Staring ...");
            cache1.invoke("A", new DeadlockProcessor());            
            println("Finished");
            
            
            while(true) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }       
    }

    public static class DeadlockProcessor extends AbstractProcessor implements Serializable {

        @Override
        public Object process(Entry entry) {
            NamedCache cache= CacheFactory.getCache("objects2");
//            cache.put(entry.getKey(), entry.getKey());
            cache.get(entry.getKey());
            entry.setValue(entry.getKey());
            return entry.getKey();
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
