/**
 * Copyright 2010 Grid Dynamics Consulting Services, Inc.
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

package org.gridkit.coherence.integration.spring.service;

/**
 * @author Dmitri Babaev
 */
public class InvocationServiceConfiguration extends GenericServiceConfiguration {

	@XmlConfigProperty("thread-count")
	protected Integer threadCount;
	
	@XmlConfigProperty("task-timeout")
	protected Integer taskTimeoutMillis;

	@XmlConfigProperty("task-hung-threshold")
	protected Integer taskHungThresholdMillis;
	
	@XmlConfigProperty("request-timeout")
	protected Integer requestTimeout;
	
	public ServiceType getServiceType() {
		return ServiceType.Invocation;
	}
	
	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	public void setTaskHungThresholdMillis(int taskHungThresholdMillis) {
		this.taskHungThresholdMillis = taskHungThresholdMillis;
	}

	public void setTaskTimeoutMillis(int taskTimeoutMillis) {
		this.taskTimeoutMillis = taskTimeoutMillis;
	}

	public void setRequestTimeout(int requestTimeout) {
		this.requestTimeout = requestTimeout;
	}
}
