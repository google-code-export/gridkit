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
package org.gridkit.drc.coherence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.gridkit.coherence.util.classloader.Isolate;
import org.gridkit.coherence.util.classloader.NodeActions;
import org.gridkit.drc.coherence.DistributedResourceCoordinator;
import org.gridkit.drc.coherence.ResourceHandler;
import org.gridkit.drc.coherence.RoleBasedShareCalculator;
import org.gridkit.drc.coherence.ShareCalculator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

/**
 * Integration DRC test, using role-based share implementation to calculate fair numbers
 * 
 * @see DistributedResourceCoordinator
 * @see RoleBasedShareCalculator
 * 
 * @author malekseev
 * 20.04.2011
 */
public class RoleBasedArbiterTest {

	private static enum Sources { S1, S2, S3, S4, S5, S6 };
	
	@BeforeClass
	public static void initCoherence() {
		// TODO 
		System.setProperty("tangosol.coherence.wka", "localhost");
		System.setProperty("tangosol.coherence.localhost", "localhost");
		System.setProperty("tangosol.coherence.ttl", "0");
		System.setProperty("tangosol.coherence.distributed.localstorage", "true");
	}
	
	@Test
	public void test_LiveBalancing() throws InterruptedException {
		Isolate is1 = new Isolate("node-1", "com.tangosol", "org.gridkit");
		Isolate is2 = new Isolate("node-2", "com.tangosol", "org.gridkit");
		Isolate is3 = new Isolate("node-3", "com.tangosol", "org.gridkit");
		
		is1.start();
		is2.start();
		is3.start();
		
		String config = getClass().getResource("/test-cache-config.xml").toExternalForm();
		
		is1.submit(Start.class, "NODE-1", config);
		is2.submit(Start.class, "NODE-2", config);
		is3.submit(Start.class, "NODE-3", config);

		Thread.sleep(10 * 1000L); // wait for cluster formation
		
		is1.submit(StartArbiter.class, "control-cache");
		is2.submit(StartArbiter.class, "control-cache");
		is3.submit(StartArbiter.class, "control-cache");
		
		Thread.sleep(3 * 1000L); // wait for sources rebalancing
		
		is1.submit(CheckArbiterState.class);
		is2.submit(CheckArbiterState.class);
		is3.submit(CheckArbiterState.class);
		
		Assert.assertEquals("2", System.getProperty("NODE-1-activeCount"));
//		Assert.assertEquals("0", System.getProperty("NODE-1-standbyCount"));
		
		Assert.assertEquals("2", System.getProperty("NODE-2-activeCount"));
//		Assert.assertEquals("0", System.getProperty("NODE-2-standbyCount"));
		
		Assert.assertEquals("2", System.getProperty("NODE-3-activeCount"));
//		Assert.assertEquals("0", System.getProperty("NODE-3-standbyCount"));
		
		is3.submit(Stop.class);
		Thread.sleep(3 * 1000L); // wait for sources rebalancing
		
		is1.submit(CheckArbiterState.class);
		is2.submit(CheckArbiterState.class);
		
		Assert.assertEquals("3", System.getProperty("NODE-1-activeCount"));
//		Assert.assertEquals("3", System.getProperty("NODE-1-standbyCount"));
		
		Assert.assertEquals("3", System.getProperty("NODE-2-activeCount"));
//		Assert.assertEquals("3", System.getProperty("NODE-2-standbyCount"));
		
		is2.submit(Stop.class);
//		is2.submit(NodeActions.Crash.class, 1);
		Thread.sleep(3 * 1000L); // wait for sources rebalancing
		
		is1.submit(CheckArbiterState.class);
		
		Assert.assertEquals("6", System.getProperty("NODE-1-activeCount"));
		Assert.assertEquals("0", System.getProperty("NODE-1-standbyCount"));
		
		is1.submit(Stop.class);
	}
	
	public static class NodeHolder {
		static String nodeId;
		static DistributedResourceCoordinator arbiterInstance;
	}
	
	public static class StartArbiter implements Runnable {
		private final String lockCacheName;
		public StartArbiter(String lockCacheName) {
			this.lockCacheName = lockCacheName;
		}
		@Override
		public void run() {
			NamedCache cache = CacheFactory.getCache(lockCacheName);
			MockResourceControl manager = new MockResourceControl(Arrays.asList(Sources.values()));
			ShareCalculator fairShare = new RoleBasedShareCalculator();
			DistributedResourceCoordinator arbiter = new DistributedResourceCoordinator();
			NodeHolder.arbiterInstance = arbiter;
			arbiter.setLockMap(cache);
			arbiter.setResources(manager.getResourcesList());
			arbiter.setResourceHandler(manager);
			arbiter.setShareCalculator(fairShare);
			arbiter.setLockCheckPeriodMillis(100); // 10 per sec
			arbiter.setRebalancePeriodMillis(500); // 2 per sec
			arbiter.start();
		}
	}
	
	public static class CheckArbiterState implements Runnable {
		@Override
		public void run() {
			System.out.print(NodeHolder.nodeId + ": ");
			NodeHolder.arbiterInstance.printStatus();
			System.setProperty(NodeHolder.nodeId+"-activeCount", String.valueOf(NodeHolder.arbiterInstance.getActiveCount()));
			System.setProperty(NodeHolder.nodeId+"-standbyCount", String.valueOf(NodeHolder.arbiterInstance.getStandByCount()));
		}
	}
	
	public static class Start extends NodeActions.Start {
		private final String node;
		public Start(String node, String config) {
			super(config);
			this.node = node;
		}
		@Override
		public void run() {
			NodeHolder.nodeId = node;
			super.run();
		}
	}
	
	public static class Stop extends NodeActions.Stop {
		@Override
		public void run() {
			NodeHolder.arbiterInstance.stop();
			super.run();
		}
	}
	
	static class MockResourceControl implements ResourceHandler {

		private List<Object> sources = new ArrayList<Object>();
		private List<Object> activeSources = new ArrayList<Object>();

		public MockResourceControl(Collection<?> sources) {
			this.sources.addAll(sources);
		}

		public boolean isActive(Object source) {
			return activeSources.contains(source);
		}

		@Override
		public void connect(Object source) {
			activeSources.add(source);
		}

		@Override
		public void disconnect(Object source) {
			activeSources.remove(source);
		}

		@Override
		public void terminate(Object resourceId) {
		}

		public Collection<Object> getResourcesList() {
			return sources;
		}
	}
	
}
