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

import junit.framework.Assert;

import org.gridkit.coherence.util.classloader.Isolate;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultConfigurableCacheFactory;

/**
 * Integration test with two cluster nodes
 * 
 * @author malekseev
 * 05.04.2011
 */
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
		
		is1.submit(StartNode.class.getName());
		is2.submit(StartNode.class.getName());
		
		is1.submit(TouchCache.class.getName());
		is2.submit(TouchCache.class.getName());
		
		Thread.sleep(5 * 1000L);
		is2.submit(NodeCrash.class.getName());
		Thread.sleep(50 * 1000L);
		
		is1.submit(StopNode.class.getName());
		
		Assert.assertEquals("true", System.getProperty("ListenerTest.lossDetected"));
	}
	
	@AfterClass
	public static void shutdown() {
		CacheFactory.getCluster().shutdown();
	}
	
	public static class StartNode implements Runnable {
		@Override
		public void run() {
			CacheFactory.setConfigurableCacheFactory(new DefaultConfigurableCacheFactory("test-canary-cache-config.xml"));
			CacheFactory.ensureCluster();
		}
	}
	
	public static class TouchCache implements Runnable {
		@Override
		public void run() {
			CacheFactory.getCache(PartitionLossListener.CACHE_NAME);
		}
	}
	
	public static class NodeCrash implements Runnable {
		@Override @SuppressWarnings("deprecation")
		public void run() {
			ThreadGroup parent = Thread.currentThread().getThreadGroup();
			ThreadGroup[] childs = new ThreadGroup[parent.activeGroupCount()];
			parent.enumerate(childs, false);
			
			ThreadGroup cluster2 = childs[1];
			cluster2.stop();
		}
	}
	
	public static class StopNode implements Runnable {
		@Override
		public void run() {
			CacheFactory.getCluster().shutdown();
		}
	}
	
}
