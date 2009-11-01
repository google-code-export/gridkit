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
package com.griddynamics.gridkit.coherence.benchmark.event;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;

public class MapListenerBackend {

	public static void main(String[] args) {
		try {

			System.setProperty("tangosol.coherence.cacheconfig", "event-benchmark-cache-config.xml");
			
			final NamedCache out = CacheFactory.getCache("out-pool");
			final NamedCache in = CacheFactory.getCache("in-pool");
			
			out.addMapListener(new MapListener() {
			
				@Override
				public void entryUpdated(MapEvent evt) {
				}
			
				@Override
				public void entryInserted(MapEvent evt) {
					in.put(evt.getKey(), evt.getNewValue());
				}
			
				@Override
				public void entryDeleted(MapEvent evt) {
				}
			});

			System.out.println("Map listener back end started");
			
			while(true) {
				Thread.sleep(100);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	};
}
