/**
 * Copyright 2013 Alexey Ragozin
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
package org.gridkit.coherence.check;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.coherence.chtest.CohCloudRule;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.CacheFactory;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;

public class BMCheck {

	@Rule
	public CohCloudRule cloud = new DisposableCohCloud();
	
	private ExecutorService executor = Executors.newCachedThreadPool();

	@After
	public void shutdown_cluster_after_test() {
		executor.shutdownNow();
	}
	
	@Test
	public void touch_vanila_cache() throws InterruptedException, ExecutionException {

		cloud.all().presetFastLocalCluster();
		cloud.all().cacheConfig("bm-check-cache-config.xml");
		
		CohNode storage = cloud.node("storage");
		storage.localStorage(true);

		storage.exec(new Runnable() {
			@Override
			public void run() {
				CacheFactory.getCache("vanila-a").addMapListener(new Listener2());
			}
		});
		storage.getCache("vanila-a").put("A", "A");
	}

	@Test
	public void touch_partitioned_cache() throws InterruptedException, ExecutionException {
		
		cloud.all().presetFastLocalCluster();
		cloud.all().cacheConfig("bm-check-cache-config.xml");
		
		CohNode storage = cloud.node("storage");
		storage.localStorage(true);
		
		storage.getCache("partitioned-a").put("A", "A");
	}
	
	public static class Listener implements MapListener {

		public Listener(BackingMapManagerContext ctx, String name) {
			System.out.println(ctx);
			System.out.println(name);
		}
		
		@Override
		public void entryDeleted(MapEvent e) {
			System.out.println(e);
		}

		@Override
		public void entryInserted(MapEvent e) {
			System.out.println(e);
		}

		@Override
		public void entryUpdated(MapEvent e) {
			System.out.println(e);
		}
	}
	public static class Listener2 implements MapListener {
		
		@Override
		public void entryDeleted(MapEvent e) {
			System.out.println(e);
		}
		
		@Override
		public void entryInserted(MapEvent e) {
			System.out.println(e);
		}
		
		@Override
		public void entryUpdated(MapEvent e) {
			System.out.println(e);
		}
	}
}
