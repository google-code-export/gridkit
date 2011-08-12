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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ScheduledResourceJobHandler} manages execution of jobs via {@link ScheduledThreadPoolExecutor}.
 * Component is designed to work in IoC container. Setters marked as required should be initialized before start using this component.
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class ScheduledResourceJobHandler implements ResourceHandler {

	private static final Logger log = LoggerFactory.getLogger(ScheduledResourceJobHandler.class);
	
	private ScheduledExecutorService executionService;
	private Map<Object, ScheduledResourceJob> jobs;
	
	private Map<Object, TaskWrapper> activeJobs = new HashMap<Object, ScheduledResourceJobHandler.TaskWrapper>();
	
	/**
	 * <b>Required</b>
	 */
	public void setJobs(Collection<ScheduledResourceJob> jobs) {
		this.jobs = new HashMap<Object, ScheduledResourceJob>();
		
		for(ScheduledResourceJob job : jobs) {
			if (this.jobs.put(job.getResourceId(), job) != null) {
				throw new IllegalArgumentException("Duplicated resource id: " + job.getResourceId());
			}
		}
	}
	
	/**
	 * <b>Required</b>
	 */
	public void setExecutionService(ScheduledExecutorService executionService) {
		this.executionService = executionService;
	}
	
	public Set<Object> getResources() {
		return Collections.unmodifiableSet(jobs.keySet());
	}
	
	@Override
	public synchronized void connect(Object resourceId) {
		if (!jobs.containsKey(resourceId)) {
			throw new IllegalArgumentException("Unknown resource ID: " + resourceId);
		}
		if (activeJobs.containsKey(resourceId)) {
			throw new IllegalStateException("Resource " + resourceId + " is already connected");
		}
		
		ScheduledResourceJob job = jobs.get(resourceId);
		job.connect();
		
		createTask(resourceId, job);
	}

	private void createTask(Object resourceId,	ScheduledResourceJob job) {
		TaskWrapper tw = new TaskWrapper();
		tw.job = job;
		tw.policy = job.getSchedulingPolicy();
		
		activeJobs.put(resourceId, tw);
		executionService.execute(tw);
	}

	@Override
	public synchronized void disconnect(Object resourceId) {
		if (!jobs.containsKey(resourceId)) {
			throw new IllegalArgumentException("Unknown resource ID: " + resourceId);
		}
		if (!activeJobs.containsKey(resourceId)) {
			throw new IllegalStateException("Resource " + resourceId + " is not connected");
		}
		
		stopTask(activeJobs.get(resourceId));
		activeJobs.remove(resourceId);
		
		jobs.get(resourceId).disconnect();
	}

	private void stopTask(TaskWrapper tw) {
		// if task is executed, thread will be blocked until its completion
		synchronized (tw) {
			tw.terminate = true;
			tw.job = null;
			tw.policy = null;
		}
	}

	@Override
	public synchronized void terminate(Object resourceId) {
		// TODO optional thread interrupt
		disconnect(resourceId);
	}

	private class TaskWrapper implements Runnable {

		private boolean firstLaunch = true;
		private SchedulingPolicy policy;
		private ScheduledResourceJob job;
		private boolean terminate;

		@Override
		public synchronized void run() {
			
			if (terminate) {
				return;
			}
			
			boolean bySchedule = !firstLaunch;
			firstLaunch = false;
			if (policy == null) {
				policy = job.getSchedulingPolicy();
			}

			try {
				try {
					policy.taskStarted();
					try {
						job.execute(bySchedule);
					}
					catch(Exception e) {
						log.error("Job " + job.toString() + " exception during execution", e);
					}
				}
				finally {
					policy.taskStarted();
				}
				
				long nextRun = policy.getTimeForNextSchedule(TimeUnit.MILLISECONDS);
				executionService.schedule(this, nextRun, TimeUnit.MILLISECONDS);
			}
			catch(Exception e) {
				log.error("Exception mess", e);
			}
		}
	}
}
