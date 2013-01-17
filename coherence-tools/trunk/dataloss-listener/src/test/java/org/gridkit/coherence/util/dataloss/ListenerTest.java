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
package org.gridkit.coherence.util.dataloss;

import org.gridkit.coherence.util.classloader.Isolate;
import org.gridkit.coherence.util.classloader.NodeActions;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tangosol.net.CacheFactory;

/**
 * Integration test with two cluster nodes
 * 
 * @author malekseev
 * 05.04.2011
 */
@SuppressWarnings("deprecation")
public class ListenerTest {
	
	@BeforeClass
	public static void initCache() {
		System.setProperty("tangosol.coherence.wka", "localhost");
		System.setProperty("tangosol.coherence.localhost", "localhost");
		System.setProperty("tangosol.coherence.distributed.localstorage", "true");
	}
	
	@Test
	public void twoNodes() throws InterruptedException {
		Isolate is1 = new Isolate("node-1", "com.tangosol", "org.gridkit");
		Isolate is2 = new Isolate("node-2", "com.tangosol", "org.gridkit");
		
		is1.start();
		is2.start();
		
		is1.submit(NodeActions.Start.class, "test-canary-cache-config.xml");
		is2.submit(NodeActions.Start.class, "test-canary-cache-config.xml");
		
		is1.submit(TouchCache.class);
		is2.submit(TouchCache.class);
		
		Thread.sleep(10 * 1000L); // FIXME try JMX waiters based on cluster statuses
		is2.submit(NodeActions.Crash.class, 1);
		Thread.sleep(5 * 1000L); // FIXME try JMX waiters based on cluster statuses
		
		is1.submit(NodeActions.Stop.class);
		
		Assert.assertEquals("true", System.getProperty("DistributedCache-1"));
		Assert.assertEquals("true", System.getProperty("DistributedCache-2"));
	}
	
	public static class TouchCache implements Runnable {
		@Override
		public void run() {
			CacheFactory.getCache("canary-cache-1");
			CacheFactory.getCache("canary-cache-2");
		}
	}
	
}
