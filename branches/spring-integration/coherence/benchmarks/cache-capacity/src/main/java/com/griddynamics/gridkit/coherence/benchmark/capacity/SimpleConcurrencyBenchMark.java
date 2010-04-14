/**
 * Copyright 2008-2009 Grid Dynamics Consulting Services, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.griddynamics.gridkit.coherence.benchmark.capacity;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import sample.DomainObjKey;
import sample.SimpleDomainObjGenerator;

import com.griddynamics.gridkit.coherence.benchmark.capacity.objects.ObjectGenerator;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.NearCache;
import com.tangosol.util.Filter;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class SimpleConcurrencyBenchMark {
	
    static AtomicLong READ_OPS = new AtomicLong();
    static AtomicLong WRITE_OPS = new AtomicLong();
    
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
//	    System.setProperty("benchmark-default-scheme", "local-scheme");
//	    System.setProperty("benchmark-default-scheme", "local-hashmap-scheme");
//	    System.setProperty("benchmark-default-scheme", "local-juc-hashmap-scheme");
//	    System.setProperty("benchmark-default-scheme", "simple-distributed-scheme");
//	    System.setProperty("benchmark-default-scheme", "simple-near-scheme");
//	    System.setProperty("benchmark-default-scheme", "external-distributed-scheme");
//	    System.setProperty("benchmark-default-scheme", "simple-replicated-scheme");
	    System.setProperty("benchmark-default-scheme", "simple-optimistic-scheme");
//	    System.setProperty("benchmark-default-scheme", "hash-map-replicated-scheme");
//	    System.setProperty("benchmark-default-scheme", "replicated-near-scheme");
	    
	    //ExecutorService executor = Executors.newCachedThreadPool();
	    
		try {
			final NamedCache cache = CacheFactory.getCache("objects");
//			final Map map = cache;
//			final Map map = new ContinuousQueryCache(cache, new ClassFilter(DomainObject.class), true);
//			final Map map = new ConcurrentHashMap();
//			final Map map = new HashMap();
			final Map map = new ConcurrentSkipListMap();
//			final Map map = Collections.synchronizedMap(new HashMap());
			final ObjectGenerator<?, ?> generator = new SimpleDomainObjGenerator();
		
//			cache.addIndex(new ReflectionExtractor("getA0"), false, null);
//			cache.addIndex(new ReflectionExtractor("getAs"), false, null);			
			
//			long objectCount = 1000000;
			long objectCount = 100000;
			
			long rangeStart = 0;
			long rangeFinish = objectCount;
			
			println("Loading " + objectCount + " objects ...");
			for(long i = rangeStart;  i < rangeFinish; i += 100) {
			    if (i % 100000 == 0) {
			        println("Done " + (i - rangeStart));
			    }
			    long j = Math.min(rangeFinish, i + 100);
			    map.putAll(generator.generate(i, j));
			}			
			
			println("Loaded " + cache.size() + " objects");
			System.gc();
			println("Mem. usage " + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());

//			checkAccess(cache, new EqualsFilter("getA0", new DomainObjAttrib("?")));
//			checkAccess(cache, new EqualsFilter("getAs", Collections.EMPTY_LIST));
//			checkAccess(cache, new ContainsAnyFilter("getAs", Collections.singleton(new DomainObjAttrib("?"))));
			
//			ContinuousQueryCache view = new ContinuousQueryCache(cache, new EqualsFilter("getHashSegment", 0));
//			System.out.println("View size " + view.size());
			
//			view.addIndex(new ReflectionExtractor("getA0"), false, null);
//            checkAccess(view, new EqualsFilter("getA0", new DomainObjAttrib("?")));
//            checkAccess(view, new EqualsFilter("getA1", new DomainObjAttrib("?")));
			
			if (map instanceof NearCache) {

			    println("Warming up near cache");
    			for(long i = rangeFinish-1; i >= rangeStart; --i) {
    			    map.get(new DomainObjKey(i));
    			}
			}
			
			println("Starting access threads");

			testHotSpot(map, 100000);
			testHotSpot(map, 1000);
			testHotSpot(map, 100);
			testHotSpot(map, 10);
			testHotSpot(map, 1);

//			for(int i = 0; i != 0; ++i) {
//			    executor.execute(new AccessThread(map, 10, 1, 0, 100));
//			}
//
//			for(int i = 0; i != 0; ++i) {
//			    executor.execute(new AccessThread(map, 100, 1, 0, 100));
//			}
//
//			for(int i = 0; i != 0; ++i) {
//			    executor.execute(new AccessThread(map, 100000, 1, 0, 100));
//			}
//			
//			READ_OPS.getAndSet(0);
//			WRITE_OPS.getAndSet(0);
//			while(true) {
//                Thread.sleep(10000);
//				
//				long reads = READ_OPS.getAndSet(0);
//				long writes = WRITE_OPS.getAndSet(0);
//				
//				println("Read rate: " + reads/10.0 + " op/sec");
//				println("Write rate: " + writes/10.0 + " op/sec");
//			}
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

    private static void testHotSpot(Map map, int hotSpot) throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();
        for(int i = 0; i != 16; ++i) {
            executor.execute(new AccessThread(map, hotSpot, 1, 0, 100));
            if (i == 0) {
                Thread.sleep(5000);
            }
            else {
                Thread.sleep(1000);
            }
            READ_OPS.getAndSet(0);
            WRITE_OPS.getAndSet(0);

            Thread.sleep(10000);
            
            long reads = READ_OPS.getAndSet(0);
            long writes = WRITE_OPS.getAndSet(0);
            
            println("Data subset " + hotSpot);
            println("Thread count " + (i + 1));
            println("Read rate: " + reads/10.0 + " op/sec");
            println("Write rate: " + writes/10.0 + " op/sec");
        }
        
        executor.shutdownNow();
        Thread.sleep(1000);
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
    
    private static class AccessThread implements Runnable {

        private Map cache;
        private int maxKey;
        private int segmentCount;
        private int segmentNo;
        private int read2write;
        
        public AccessThread(Map cache, int maxKey, int segmentCount, int segmentNo, int read2write) {
            this.cache = cache;
            this.maxKey = maxKey;
            this.segmentCount = segmentCount;
            this.segmentNo = segmentNo;
            this.read2write = read2write;
        }

        @Override
        public void run() {
            Random rnd = new Random();
            while(true) {
                if (Thread.interrupted()) {
                    break;
                }
                int key = rnd.nextInt(maxKey);
                key /= segmentCount;
                key = key * segmentCount + segmentNo;

                if (rnd.nextInt(100) > read2write) {
                    Object obj = cache.get(new DomainObjKey(key));
                    cache.put(new DomainObjKey(key), obj);
                    WRITE_OPS.incrementAndGet();
                }
                else {
//                    synchronized(cache) {
                        Object obj = cache.get(new DomainObjKey(key));
//                    }
                    READ_OPS.incrementAndGet();
                }
            }
        }
    }
}
