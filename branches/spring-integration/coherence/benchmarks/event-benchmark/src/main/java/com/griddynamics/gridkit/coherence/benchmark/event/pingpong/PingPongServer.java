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
package com.griddynamics.gridkit.coherence.benchmark.event.pingpong;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class PingPongServer {

	public static void main(String[] args) {
		try {

			System.setProperty("tangosol.coherence.cacheconfig", "event-benchmark-cache-config.xml");

			// ***
			System.setProperty("event-benchmark-thread-count", "4");
			
			final NamedCache p1 = CacheFactory.getCache("pool-1");
			final NamedCache p2 = CacheFactory.getCache("pool-2");
			final NamedCache p3 = CacheFactory.getCache("pool-3");
			final NamedCache p4 = CacheFactory.getCache("pool-4");
			final NamedCache pin = CacheFactory.getCache("in-pool");
			final NamedCache pout = CacheFactory.getCache("out-pool");
			
			System.out.println("Storage node started");
			
			while(true) {
				Thread.sleep(100);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	};
}
