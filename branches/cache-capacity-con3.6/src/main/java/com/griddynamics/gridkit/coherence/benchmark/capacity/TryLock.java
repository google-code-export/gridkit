package com.griddynamics.gridkit.coherence.benchmark.capacity;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.NearCache;
import com.tangosol.util.ConcurrentMap;
import com.tangosol.util.Filter;

public class TryLock {
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
        
        System.setProperty("benchmark-default-scheme", "near-distributed-scheme");     
        
        try {
            NearCache cache = (NearCache) CacheFactory.getCache("objects");
            
            cache.release();
            cache = (NearCache) CacheFactory.getCache("objects");
            
            ConcurrentMap cm = cache.getControlMap();
            ExecutorService service = Executors.newFixedThreadPool(1);
            
            cache.put("A", "A");
            cache.get("A");
            cache.put("B", "A");
            cache.get("B");
            cache.put("C", "A");
            cache.get("C");
            cache.put("D", "A");
            cache.get("D");
            
            cache.getControlMap().lock(ConcurrentMap.LOCK_ALL, -1);
//            cache.getBackCache().put("D", "D");
            cache.getFrontMap().clear();
            cache.getControlMap().clear();
            cache.getControlMap().unlock(ConcurrentMap.LOCK_ALL);
            
            cache.get("A");
            cache.put("B", "B");
            
            println("[A] under lock");
            cm.lock("A");
            println("tryLock(A) -> " + service.submit(new TryLockCallable(cm, "A")).get());
            println("tryLock(B) -> " + service.submit(new TryLockCallable(cm, "B")).get());
            println("tryLock(ALL) -> " + service.submit(new TryLockCallable(cm, ConcurrentMap.LOCK_ALL)).get());
            cm.unlock("A");
            cm.lock(ConcurrentMap.LOCK_ALL);
            println("[ALL] under lock");
            println("tryLock(A) -> " + service.submit(new TryLockCallable(cm, "A")).get());
            println("tryLock(B) -> " + service.submit(new TryLockCallable(cm, "B")).get());
            println("tryLock(ALL) -> " + service.submit(new TryLockCallable(cm, ConcurrentMap.LOCK_ALL)).get());
            
        } catch (Exception e) {
            e.printStackTrace();
        }       
    }

    private static class TryLockCallable implements Callable<Boolean> {

        private final ConcurrentMap map;
        private final Object key;
        
        public TryLockCallable(ConcurrentMap map, Object key) {
            this.map = map;
            this.key = key;
        }

        @Override
        public Boolean call() throws Exception {
            if (map.lock(key, 300)) {
                map.unlock(key);
                return true;
            }
            else {
                return false;
            }
        }
        
    };
    
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
