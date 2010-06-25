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
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import sample.DomainObjAttrib;
import sample.SimpleDomainObjGenerator;

import com.tangosol.coherence.transaction.Connection;
import com.tangosol.coherence.transaction.DefaultConnectionFactory;
import com.tangosol.coherence.transaction.OptimisticNamedCache;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.EqualsFilter;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class NonTxnIndexTest {
	
	static void println() {
	    System.out.println();
	}
	
	static void println(String text) {
	    System.out.println(String.format("[%1$tH:%1$tM:%1$tS.%1$tL] ", new Date()) + text);
	}
	
	public static void main(String[] args) {
	    
	    System.setProperty("tangosol.pof.enabled", "false");
//	    System.setProperty("tangosol.pof.config", "capacity-benchmark-pof-config.xml");
	    System.setProperty("tangosol.coherence.cacheconfig", "capacity-benchmark-cache-config.xml");
        System.setProperty("benchmark-default-scheme", "simple-distributed-scheme");
	    
	    
		try {
	
		    final NamedCache cache = CacheFactory.getCache("objects");			
			final SimpleDomainObjGenerator generator = new SimpleDomainObjGenerator();
			
			cache.addIndex(new ReflectionExtractor("getA0"), false, null);

			int putSize = 1000;
			long objectCount = 100000;

			
			long rangeStart = 1000000;
			long rangeFinish = rangeStart + objectCount;
			
			long startNs = System.nanoTime();
			println("Loading " + objectCount + " objects ...");
			long blockTs = System.nanoTime();
			long blockStart = rangeStart;
			for(long i = rangeStart;  i < rangeFinish; i += putSize) {
			    if (i % 100 == 0) {
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
		        cache.putAll(generator.generate(i, j));
			}			
			
			long totalTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

			println("Loaded " + cache.size() + " objects");
			println("Loading time " + totalTime + "ms");
			System.gc();
			println("Mem. usage " + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());

	        println("Indexed filter");
			checkAccess(cache, new EqualsFilter("getA0", new DomainObjAttrib("?")));
			println("Non-indexed filter");
			checkAccess(cache, new EqualsFilter("getA1", new DomainObjAttrib("?")));
			
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
