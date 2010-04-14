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
package com.griddynamics.convergence.fabric.remote;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.griddynamics.convergence.demo.dar.service.GridScheduler;
import com.griddynamics.convergence.demo.utils.cluster.GridHostMap;
import com.griddynamics.convergence.demo.utils.exec.ExecCommand;
import com.griddynamics.convergence.demo.utils.exec.GridProcessScheduler;
import com.griddynamics.convergence.demo.utils.exec.ProcessExecutor;

public class ProcessGridSchedulerAdaptor implements GridScheduler {

	private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
	private static final AtomicInteger TASK_COUNTER = new AtomicInteger();
	
	private GridProcessScheduler scheduler;
	private ExecCommand javaCmd;
	private GridHostMap hostMap;
	
	public ProcessGridSchedulerAdaptor(GridProcessScheduler scheduler, ExecCommand javaCmd) {
		this.scheduler = scheduler;
		this.javaCmd = javaCmd;
	}
	
	public void setHostMap(GridHostMap hostMap) {
		this.hostMap = hostMap;
	}
	
	public <T> Future<T> submit(final String host, final Callable<T> task) {
		
		final int taskId = TASK_COUNTER.getAndIncrement();
		
		try {
			// TODO process termination watch dog
			Callable<T> wrapper = new Callable<T>() {

				public T call() throws Exception {
					String targetHost = host; 
					if (hostMap != null && host != null) {
						targetHost = hostMap.normalizeHost(targetHost);
					}
					ExecutorService service = null;
					try {
						ProcessExecutor pexec = scheduler.getAllignedExecutor(targetHost);
						service = RemoteJvmHelper.createRemoteExecutor(pexec, "[" + targetHost + ":task#" + taskId + "] ", javaCmd);
//						System.out.println("Task #" + taskId + " started");
//						System.out.println("Sending task #" + taskId + " data ...");
						T result = service.submit(task).get();
//						System.out.println("Task #" + taskId + " done");
						return result;
					}
					finally {
						try {
							if (service != null) {
								service.shutdown();
							}
						}
						catch(Exception e) {
							// ignore
						}
					}
				}
			};			
			return EXECUTOR.submit(wrapper);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
