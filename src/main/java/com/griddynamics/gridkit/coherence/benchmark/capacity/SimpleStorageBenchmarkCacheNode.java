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
import java.util.concurrent.TimeUnit;

import sample.SimpleDomainObjGenerator;

import com.griddynamics.gridkit.coherence.benchmark.capacity.objects.ObjectGenerator;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class SimpleStorageBenchmarkCacheNode {
	
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
	    
//	    System.setProperty("benchmark-default-scheme", "local-scheme");	    
//	    System.setProperty("benchmark-default-scheme", "local-hashmap-scheme");
//	    System.setProperty("benchmark-default-scheme", "local-juc-hashmap-scheme");
//	    System.setProperty("benchmark-default-scheme", "simple-distributed-scheme");
	    System.setProperty("benchmark-default-scheme", "simple-distributed-quorum-scheme");
//	    System.setProperty("benchmark-default-scheme", "external-distributed-scheme");
//	    System.setProperty("benchmark-default-scheme", "partitioned-true-external-distributed-scheme");
//	    System.setProperty("benchmark-default-scheme", "partitioned-false-external-distributed-scheme");
//	    System.setProperty("benchmark-default-scheme", "simple-replicated-scheme");
//	    System.setProperty("benchmark-default-scheme", "transactional-scheme");	    
	    
		try {
			final NamedCache cache = CacheFactory.getCache("objects");

			println("Cache node has started");
			
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
