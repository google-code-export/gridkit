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

import com.griddynamics.gridkit.coherence.benchmark.capacity.objects.ObjectGenerator;
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
public class TxnConflictTest {
	
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
	    System.setProperty("benchmark-default-scheme", "transactional-scheme");	    
	    
		try {
	
		    final NamedCache cache = CacheFactory.getCache("objects");			
			final Connection cacheCon1= new DefaultConnectionFactory().createConnection();
			final Connection cacheCon2= new DefaultConnectionFactory().createConnection();
						
			final OptimisticNamedCache cacheTxn1 = cacheCon1.getNamedCache(cache.getCacheName());
			cacheCon1.setAutoCommit(false);
			final OptimisticNamedCache cacheTxn2 = cacheCon2.getNamedCache(cache.getCacheName());
			cacheCon1.setAutoCommit(false);

			cacheTxn1.put("test", "test");
			cacheCon1.commit();

			cacheTxn1.get("test");
			cacheTxn1.put("test", "test1");

			cacheTxn2.get("test");
			cacheTxn2.put("test", "test2");
						
			System.out.println("C:  -> " + cache.get("test"));
			System.out.println("C1: -> " + cacheTxn1.get("test"));
			System.out.println("C2: -> " + cacheTxn2.get("test"));
			
			System.out.println("Commit");
			cacheCon1.commit();
			cacheCon2.commit();

			System.out.println("C:  -> " + cache.get("test"));
            System.out.println("C1: -> " + cacheTxn1.get("test"));
            System.out.println("C2: -> " + cacheTxn2.get("test"));
			
			
			while(true) {    
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
