package com.griddynamics.gridkit.coherence.benchmark.capacity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.ReadWriteBackingMap;
import com.tangosol.net.cache.ReadWriteBackingMap.CacheStoreWrapper;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.Filter;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.InvocableMap.EntryProcessor;
import com.tangosol.util.processor.AbstractProcessor;
import com.tangosol.util.processor.PreloadRequest;

public class CacheStoreBundlingTest {

    static void println() {
        System.out.println();
    }
    
    static void println(String text) {
        System.out.println(String.format("[%1$tH:%1$tM:%1$tS.%1$tL] ", new Date()) + text);
    }
    
    public static void main(String[] args) {
        
        System.setProperty("tangosol.pof.enabled", "false");
        System.setProperty("tangosol.pof.config", "capacity-benchmark-pof-config.xml");
        System.setProperty("tangosol.coherence.cacheconfig", "capacity-benchmark-cache-config.xml");
        System.setProperty("tangosol.coherence.distributed.localstorage", "true");
        
        System.setProperty("benchmark-default-scheme", "read-through-distributed-scheme");
        
        try {
            final NamedCache cache = CacheFactory.getCache("objects");
            
            Object[] keys = {"A", "B", "C"};            
            getKeys(cache, Arrays.asList(keys));
            
            List list = new ArrayList();
            for(int i = 0; i != 100; ++i) {
                list.add(String.valueOf(i));
            }
            
            cache.clear();
            getKeys(cache, list);     
            cache.clear();
            preloadKeys(cache, list);
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }       
    }

    private static void getKeys(NamedCache cache, Collection keyList) {
        System.out.println("getKeys(" + keyList + ")");
        Map result = cache.getAll(keyList);
        System.out.println("result: " + new HashMap(result));        
    }

    private static void preloadKeys(NamedCache cache, Collection keyList) {
        System.out.println("preloadKeys(" + keyList + ")");
//        cache.invokeAll(keyList, new BulkPreloader35("objects"));
        cache.invokeAll(keyList, new BulkPreloader36());
        System.out.println("done. Cache size is " + cache.size());        
        System.out.println("try to getAll");
        Map result = cache.getAll(keyList);
        System.out.println("result: " + new HashMap(result));        
    }

//    public static class BulkPreloader35 extends AbstractProcessor {
//
//        private String cacheName;
//        
//        public BulkPreloader35(String cacheName) {
//            this.cacheName = cacheName;
//        }
//
//        @Override
//        public Object process(Entry entry) {
//            processAll(Collections.singleton(entry));
//            return null;
//        }
//
//        @SuppressWarnings("unchecked")
//        @Override
//        public Map processAll(Set entries) {
//            if (!entries.isEmpty()) {
//                Map backingMap = null;
//                Set<Object> keys = new HashSet<Object>(entries.size());
//                for(Object e: entries) {
//                    BinaryEntry entry = (BinaryEntry) e;
//                    if (backingMap == null) {
//                        backingMap = entry.getContext().getBackingMap(cacheName);
//                    }
//                    if (!entry.isPresent()) {
//                        keys.add(entry.getKey());
////                        keys.add(entry.getBinaryKey());
//                    }
//                }
//                
//                ReadWriteBackingMap rwmap = (ReadWriteBackingMap) backingMap;
////                rwmap.getAll(keys);
//                rwmap.getAll(colKeys)
//                Map data = rwmap.getCacheStore().getLoadBundler().loadAll(keys);
//                
//                for(Object e: entries) {
//                    BinaryEntry entry = (BinaryEntry) e;
//                    Object value = data.get(entry.getKey());
//                    if (value != null) {
//                        // read-through is suggested synthetic update
//                        entry.setValue(value, true);
//                    }
//                }
//            }
//            return Collections.EMPTY_MAP;
//        }
//    }
    
    
    public static class BulkPreloader36 extends AbstractProcessor {

        @Override
        public Object process(Entry entry) {
            processAll(Collections.singleton(entry));
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Map processAll(Set entries) {
            if (!entries.isEmpty()) {
                Map backingMap = null;
                List<Object> keys = new ArrayList<Object>(entries.size());
                for(Object e: entries) {
                    BinaryEntry entry = (BinaryEntry) e;
                    if (backingMap == null) {
                        backingMap = entry.getBackingMap();
                    }
                    if (!entry.isPresent()) {
                        keys.add(entry.getKey());
//                        keys.add(entry.getBinaryKey());
                    }
                }
                
                ReadWriteBackingMap rwmap = (ReadWriteBackingMap) backingMap;
//                rwmap.getAll(keys);
                Map data = ((CacheStoreWrapper)rwmap.getCacheStore()).getCacheStore().loadAll(keys);
                
                for(Object e: entries) {
                    BinaryEntry entry = (BinaryEntry) e;
                    Object value = data.get(entry.getKey());
                    if (value != null) {
                        // read-through is suggested synthetic update
                        entry.setValue(value, true);
                    }
                }
            }
            
            return Collections.EMPTY_MAP;
        }        
    }
}
