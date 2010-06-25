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

public class CacheStoreBundlingTest {

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
        
        System.setProperty("benchmark-default-scheme", "read-through-distributed-scheme");
        
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
    
    
    public void onGetAllRequest(PartitionedCache.GetAllRequest msgRequest) 
    { 
        PartitionedCache.PartialMapResponse msgResponse = (PartitionedCache.PartialMapResponse)super.instantiateMessage("PartialMapResponse");
        msgResponse.respondTo(msgRequest);

        Set setKeys = msgRequest.getKeySet();
        Binary[] setKeysArray = setKeys.toArray(new Binary[setKeys.size()]);
        // we may use any order, important what all operations accuring locks to multiple keys at same time
        // do what in same order
        Arrays.sort(setKeysArray, new LexicographicBinaryComparator());
        Collection sortedKeys = Arrays.asList(setKeyArray);
        PartitionedCache.Storage storage = getKnownStorage(msgRequest.getCacheId());
        if (((storage == null) ? 0 : 1) != 0)
        {
            int cKeys = setKeys.size();
            Binary[] abinKey = new Binary[cKeys];
            Binary[] abinVal = new Binary[cKeys];
            Map mapPrime = storage.getResourceMap();
            int cEntries = 0;
            int cbLimit = msgRequest.getSizeThreshold();
            int cbSize = 0;

            PartitionedService.PinningIterator pinner = super.createPinningIterator(sortedKeys);
            List lockedKeys = new ArrayList(sortedKeys.size());
            try {
                while (pinner.hasNext) {
                    try {
                        while (pinner.hasNext())
                        {
                            Binary binKey = (Binary)pinner.next();
                            if (lockKey(storage, binKey, false))
                            {
                                lockedKeys.add(binKey);

                                // break if limit of keys per bulk request is exceeded, or estimated value size may exceed cbLimit
                                ...
                            }                       
                        }

                        Map values = mapPrime.getAll(lockedKeys);
                        for(Map.Entry entry: values.entrySet()) {
                            Binary bKey = (Binary)entry.getKey();
                            Binary bValue = (Binary)entry.getValue();
                            abinKey[cEntries] = bKey;
                            abinVal[cEntries] = bValue;
                            ++cEntries;

                            // break in cbLimit is exceeded
                            ...                     
                        }                       
                    }
                    finally {
                        for(Object binKey: lockedKeys) {
                            unlockKey(storage, binKey, false);                          
                        }
                        lockedKeys.clear();
                    }
                }   
            }
            catch(Throwable e) {
                msgResponse.setException(super.tagException(e));
                cEntries = -1;
            }
            super.unpinPartitions(pinner.getPinnedPartitions());

            if (((cEntries < 0) ? 0 : 1) != 0)
            {
                msgResponse.setSize(cEntries);
                msgResponse.setKey(abinKey);
                msgResponse.setValue(abinVal);
                msgResponse.setRejectPartitions(pinner.getRejectedPartitions());
            }
        }
        else
        {
            msgResponse.setRejectPartitions(super.calculatePartitionSet(setKeys));
        }

        super.post(msgResponse); 
    }
    
}
