/**
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

import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import junit.framework.Assert;

import org.junit.Test;

import com.tangosol.net.cache.WrapperNamedCache;
import com.tangosol.util.ConcurrentMap;
import com.tangosol.util.SegmentedConcurrentMap;


public class ScheduledJobHandlerTest {

	@Test
	public void lifeCycleTest() {

		ConcurrentMap cache = new WrapperNamedCache(new SegmentedConcurrentMap(), "controlCache");
		ScheduledExecutorService executor =  new ScheduledThreadPoolExecutor(2);

		TestJob j1 = new TestJob("j1");
		TestJob j2 = new TestJob("j2");
		TestJob j3 = new TestJob("j3");
		
		ScheduledResourceJob[] jobs = {j1, j2, j3};
		ScheduledResourceJobHandler handler = new ScheduledResourceJobHandler();
		handler.setJobs(Arrays.asList(jobs));
		handler.setExecutionService(executor);
		
		DistributedResourceCoordinator coordinator = new DistributedResourceCoordinator();
		coordinator.setResources(handler.getResources());
		coordinator.setResourceHandler(handler);
		coordinator.setLockMap(cache);
		coordinator.setShareCalculator(new StaticFairShare(1));
		
		coordinator.start();
		
		LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(2));
		
		Assert.assertEquals(true, j1.connected);
		Assert.assertEquals(true, j1.initialized);
		Assert.assertEquals(false, j1.disconnected);
		Assert.assertTrue(j1.runCount > 4);

		Assert.assertEquals(true, j2.connected);
		Assert.assertEquals(true, j2.initialized);
		Assert.assertEquals(false, j2.disconnected);
		Assert.assertTrue(j2.runCount > 4);
		
		Assert.assertEquals(true, j3.connected);
		Assert.assertEquals(true, j3.initialized);
		Assert.assertEquals(false, j3.disconnected);
		Assert.assertTrue(j3.runCount > 4);

		coordinator.stop();
		
		Assert.assertEquals(true, j1.disconnected);
		Assert.assertEquals(true, j2.disconnected);
		Assert.assertEquals(true, j3.disconnected);
	}

	private class TestJob implements ScheduledResourceJob {

		String id;
		boolean connected;
		boolean disconnected;
		boolean initialized;
		int runCount;
		long interval = 300; //ms
		
		public TestJob(String id) {
			this.id = id;
		}
		
		@Override
		public Object getResourceId() {
			return id;
		}

		@Override
		public SchedulingPolicy getSchedulingPolicy() {
			return SchedulingPolicies.newPeriodicTimeTablePolicy(interval, TimeUnit.MILLISECONDS);
		}

		@Override
		public void connect() {
			connected = true;
		}

		@Override
		public void execute(boolean bySchedule) {
			if (!bySchedule) {
				initialized = true;
			}
			++runCount;
		}

		@Override
		public void disconnect() {
			disconnected = true;			
		}
	}
}
