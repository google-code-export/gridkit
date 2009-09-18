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

import com.griddynamics.gridkit.coherence.benchmark.capacity.objects.ObjectGenerator;
import com.griddynamics.gridkit.coherence.benchmark.capacity.objects.sample.SimpleDomainObjGenerator;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.extractor.ReflectionExtractor;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class SimpleStorageBenchmark {
	
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
	    System.setProperty("benchmark-default-scheme", "simple-distributed-scheme");
//	    System.setProperty("benchmark-default-scheme", "external-distributed-scheme");
//	    System.setProperty("benchmark-default-scheme", "simple-replicated-scheme");
	    
		try {
			final NamedCache cache = CacheFactory.getCache("objects");
			final ObjectGenerator<?, ?> generator = new SimpleDomainObjGenerator();
		
			cache.addIndex(new ReflectionExtractor("getA0"), false, null);
			
			long objectCount = 1000000;
			
			long rangeStart = 1000000;
			long rangeFinish = 1000000 + objectCount;
			
			println("Loading " + objectCount + " objects ...");
			for(long i = rangeStart;  i < rangeFinish; i += 100) {
			    if (i % 100000 == 0) {
			        println("Done " + (i - rangeStart));
			    }
			    long j = Math.min(rangeFinish, i + 100);
			    cache.putAll(generator.generate(i, j));
			}			
			
			println("Loaded " + cache.size() + " objects");
			System.gc();
			println("Mem. usage " + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
			
			while(true) {
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	};
}
