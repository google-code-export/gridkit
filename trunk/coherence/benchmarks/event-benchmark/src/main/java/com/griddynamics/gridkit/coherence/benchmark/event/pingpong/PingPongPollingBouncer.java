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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class PingPongPollingBouncer {
	
	public static void main(String[] args) {
		try {

			System.setProperty("tangosol.coherence.cacheconfig", "event-benchmark-cache-config.xml");
			System.setProperty("tangosol.coherence.distributed.localstorage", "false");

			// ***
			System.setProperty("event-benchmark-thread-count", "4");

			final NamedCache out = CacheFactory.getCache("out-pool");
			final NamedCache in = CacheFactory.getCache("in-pool");
 			
			System.out.println("Bouncer node started");
			
			while(true) {
				pump(out, in);
			}				
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	private static void pump(final NamedCache out, final NamedCache in) {
		Map buf = new HashMap();
		for(Object next: in.entrySet()) {
			Map.Entry entry = (Entry) next;
			buf.put(entry.getKey(), entry.getValue());
		}
		if (buf.isEmpty()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		else {
			out.putAll(buf);
			in.keySet().removeAll(buf.keySet());
		}
	};
}
