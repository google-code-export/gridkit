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

import java.util.Collection;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;

import com.tangosol.net.CacheFactory;
import com.tangosol.util.ConcurrentMap;

@Ignore
public class Sample {

}

@Ignore
class SampleResourceJob implements ScheduledResourceJob {

	private String id;
	
	//...
	
	@Override
	public Object getResourceId() {
		// an ID of resource in cluster (e.g. database URI)
		// ID should be immutable, implement hash()/equals() and be in compliance with Coherence serialization protocol
		// java.lang.String is good choice for ID
		return id;
	}

	@Override
	public SchedulingPolicy getSchedulingPolicy() {
		// SchedulingPolicy interface is very flexible
		// but you can use few out of box simple scheduling policies
		
		// this will make this job run every 15 seconds
		return SchedulingPolicies.newPeriodicTimeTablePolicy(15, TimeUnit.SECONDS);
	}

	@Override
	public void connect() {
		// you may do some initialization here
		// or you can defer it to execute call
		
	}

	@Override
	public void execute(boolean bySchedule) {
		// execute is called from thread pool, so we can take our time hear
		if (!bySchedule) {
			// we have been called just after connection to resource
			// it is a good place to initialize some long term resources such as database connection 
		}
		
		// do our job
	}

	@Override
	public void disconnect() {
		// resource is going to be disconnected
		// it is time to release long term resources (e.g. database connections) if we have allocated any
	}
}

@Ignore
class ResourceControl {
	
}

@Ignore
class Main {
	
	public void init() {
	
		// all Distributed Resource Coordination in same resource group
		// should use same Coherence cache (eigther distribute or replicated scheme)
		// but you may create multiple DRC groups using different caches
		ConcurrentMap cache = CacheFactory.getCache("some-cache-name");
		
		// All DRC instances in same group are expected to have same resource list
		Collection<ScheduledResourceJob> jobs; // you should get list of job somehow 
		jobs = null;
		
		// This thread pool will be used to run jobs
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(4);
		
		// creating a handler
		ScheduledResourceJobHandler handler = new ScheduledResourceJobHandler();
		handler.setExecutionService(executor);
		handler.setJobs(jobs);
		
		// creating DRC (minimal working setup)
		DistributedResourceCoordinator coordinator = new DistributedResourceCoordinator();
		coordinator.setLockMap(cache);
		coordinator.setResources(handler.getResources());
		coordinator.setResourceHandler(handler);
		
		// This is default share calculator which is using process role to count all DRC in cluster
		//coordinator.setShareCalculator(new RoleBasedShareCalculator());
		
		// Now minimal configuration is done and we can start DRC node
		coordinator.start();
		
		// ...
		
		// Eventually we may want to bring local DRC node down.
		// stop() method will block until, all jobs will be gracefully disconnected
		coordinator.stop();
		
	}
}