/**
 * Copyright 2011 Grid Dynamics Consulting Services, Inc.
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
package org.gridkit.coherence.util.classloader;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultConfigurableCacheFactory;

/**
 * Generic Coherence actions, submittable to Isolate instances
 * 
 * @author malekseev
 * 15.04.2011
 */
public abstract class NodeActions {
	
	/**
	 * Starts Coherence cluster node
	 * 
	 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
	 */
	public static class Start implements Runnable {
		private final String config;
		public Start(String config) { this.config = config; }

		@Override
		public void run() {
			System.setProperty("tangosol.coherence.member", Thread.currentThread().getName());
			System.setProperty("tangosol.coherence.cluster", "local-test");
			
			System.out.println(Thread.currentThread().getName() + " starting Coherence node ...");
			CacheFactory.setConfigurableCacheFactory(new DefaultConfigurableCacheFactory(config));
			CacheFactory.ensureCluster();
			System.out.println(Thread.currentThread().getName() + " Coherence node has started");
		}
	}

	/**
	 * Initializes a cache
	 * 
	 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
	 */
	public static class GetCache implements Runnable {
		private final String cacheName;
		public GetCache(String cacheName) { this.cacheName = cacheName; }
		
		@Override
		public void run() {
			System.out.println(Thread.currentThread().getName() + " initializing cache [" + cacheName + "] ...");
			CacheFactory.getCache(cacheName);
			System.out.println(Thread.currentThread().getName() + " cache [" + cacheName + "] initialized");
		}
	}

	/**
	 * Initializes a service
	 * 
	 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
	 */
	public static class GetService implements Runnable {
		private final String serviceName;
		public GetService(String serviceName) { this.serviceName = serviceName; }
		
		@Override
		public void run() {
			System.out.println(Thread.currentThread().getName() + " initializing service [" + serviceName + "] ...");
			CacheFactory.getService(serviceName);
			System.out.println(Thread.currentThread().getName() + " service [" + serviceName + "] initialized");
		}
	}
	
	/**
	 * Stops Coherence cluster node
	 * 
	 * @author aragozin
	 */
	public static class Stop implements Runnable {
		@Override
		public void run() {
			CacheFactory.getCluster().shutdown();
		}
	}
	
	/**
	 * Simulates Coherence cluster node crash, accepting node number as a parameter.
	 * Node number depends on Isolates starting order with first started equal to 0.
	 * 
	 * @author malekseev
	 */
	public static class Crash implements Runnable {

		public Crash(int node) {}

		@Override @SuppressWarnings("deprecation")
		public void run() {
			ThreadGroup parent = Thread.currentThread().getThreadGroup();
			ThreadGroup[] childs = new ThreadGroup[parent.activeGroupCount()];
			parent.enumerate(childs, false);
			
			// each Isolate has own thread group now, so Coherence cluster has always at index 0
			ThreadGroup cluster2 = childs[0];
			cluster2.stop();
		}
	}
	
}
