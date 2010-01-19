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
package com.griddynamics.convergence.demo.dar.control;

import java.rmi.RemoteException;

public class SysoutJobMonitor implements JobExecutionMonitor {

	public void jobEvent(JobEvent info) throws RemoteException {
		System.err.println("Job event ID: " + info.jobId);
		System.err.println("job started at: " + info.startTime);
		if (info.finished) {
			System.err.println("job finished at: " + info.finishTime);
		}
		else {
			System.err.println("job is running");
		}
	}

	public void taskEvent(TaskEvent info) throws RemoteException {
		System.err.println("Task event ID: " + info.taskId);
		System.err.println("Task book #" + info.bookId);
		System.err.println("Task exec host " + info.execHost);
		System.err.println("Task data host " + info.dataHost);
		System.err.println("Task started at: " + info.startTime);
		if (info.finished) {
			System.err.println("Task finished at: " + info.finishTime);
			System.err.println("Task search time: " + info.searchTime);
			System.err.println("Task fetch time: " + info.fetchTime);
		}
		else {
			System.err.println("task is running");
		}
	}
}
