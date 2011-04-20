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
package org.gridkit.coherence.util.arbiter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.gridkit.coherence.util.arbiter.DistributedResourceCoordinator.ResourceLockKey;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.tangosol.net.cache.WrapperNamedCache;
import com.tangosol.util.ConcurrentMap;
import com.tangosol.util.SegmentedConcurrentMap;

/**
 * Unit DRC test, using configurable share implementation to calculate fair numbers
 * 
 * @see DistributedResourceCoordinator
 * @see StaticFairShare
 * 
 * @author malekseev
 * 20.04.2011
 */
public class DistributedArbiterTest {
	
	private static enum Sources { S1, S2, S3, S4, S5, S6, S7 };

	private ConcurrentMap cache;
	private MockResourceControl manager;
	private DistributedResourceCoordinator coordinator;
	private ExecutorService executor;

	@Before
	public void init() {
		cache = new WrapperNamedCache(new SegmentedConcurrentMap(), "controlCache");
		manager = new MockResourceControl(Arrays.asList(Sources.values()));
		coordinator = new DistributedResourceCoordinator();
		coordinator.setDatasyncManager(manager);
		coordinator.setLockMap(cache);
		executor = Executors.newFixedThreadPool(1);
	}

	@After
	public void tearUp() throws InterruptedException, ExecutionException {
		executor.submit(new Runnable() {
			@Override
			public void run() {
				execPrivate(coordinator, "shutdown");
			}
		}).get();
		executor.shutdown();
	}

	@Test
	public void test_takeOver() throws Exception {
		StaticFairShare fsc = new StaticFairShare();
		fsc.setPeerCount(3);
		coordinator.setFairSourceCalculator(fsc);
		execPrivate(coordinator, "initSourceControls");

		call_checkLocks();

		Assert.assertEquals(4, coordinator.getActiveCount());
		Assert.assertEquals(3, coordinator.getStandByCount());

		call_checkLocks();

		Assert.assertEquals(7, coordinator.getActiveCount());
		Assert.assertEquals(0, coordinator.getStandByCount());

		for(Object source: manager.sources) {
			Assert.assertTrue("Source " + source + " should be loccked", isPrimaryLocked(source));
		}
	}

	@Test
	public void test_takeOverAlternative() throws Exception {
		StaticFairShare fsc = new StaticFairShare();
		fsc.setPeerCount(4);
		coordinator.setFairSourceCalculator(fsc);
		execPrivate(coordinator, "initSourceControls");

		call_checkLocks();

		Assert.assertEquals(3, coordinator.getActiveCount());
		Assert.assertEquals(4, coordinator.getStandByCount());

		call_checkLocks();

		Assert.assertEquals(7, coordinator.getActiveCount());
		Assert.assertEquals(0, coordinator.getStandByCount());

		for(Object source: manager.sources) {
			Assert.assertTrue("Source " + source + " should be loccked", isPrimaryLocked(source));
		}
	}

	@Test
	public void test_giveUp() throws Exception {
		StaticFairShare fsc = new StaticFairShare();
		fsc.setPeerCount(3);

		coordinator.setFairSourceCalculator(fsc);
		execPrivate(coordinator, "initSourceControls");

		call_checkLocks();
		call_checkLocks();
		Assert.assertEquals(7, coordinator.getActiveCount());

		cache.lock(new DistributedResourceCoordinator.ResourceLockKey(ResourceLockKey.KEYTYPE_STANDBY, Sources.S1));

		call_balance();
		Assert.assertEquals(6, coordinator.getActiveCount());
		Assert.assertFalse(isPrimaryLocked(Sources.S1));
	}

	private void call_checkLocks() throws InterruptedException, ExecutionException {
		executor.submit(new Runnable() {
			@Override
			public void run() {
				execPrivate(coordinator, "checkLocks");
			}
		}).get();
	};

	private void call_balance() throws InterruptedException, ExecutionException {
		executor.submit(new Runnable() {
			@Override
			public void run() {
				execPrivate(coordinator, "balance");
			}
		}).get();
	};
	
	
	private boolean isPrimaryLocked(Object source) {
		return !cache.lock(new DistributedResourceCoordinator.ResourceLockKey(ResourceLockKey.KEYTYPE_ACTIVE, source), 1);
	}

	
	private Object execPrivate(Object object, String methodName) {
		try {
			final Method methods[] = object.getClass().getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				if (methodName.equals(methods[i].getName())) {
					methods[i].setAccessible(true);
					return methods[i].invoke(object);
				}
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException("Exception while trying to call private method on DRC", e);
		}
	}
	
	
	static class MockResourceControl implements ResourceControl {

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
		public Collection<?> getResourcesList() {
			return sources;
		}
	}
}
