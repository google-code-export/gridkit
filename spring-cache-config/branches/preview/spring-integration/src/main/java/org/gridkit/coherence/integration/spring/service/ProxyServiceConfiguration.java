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

import com.tangosol.coherence.component.net.extend.proxy.serviceProxy.CacheServiceProxy;
import com.tangosol.coherence.component.net.extend.proxy.serviceProxy.InvocationServiceProxy;

/**
 * @author Max Alexejev (malexejev@gmail.com)
 * 06.09.2010
 */
public class ProxyServiceConfiguration extends GenericServiceConfiguration {
	
	@XmlConfigProperty("thread-count")
	protected Integer threadCount;
	
	@XmlConfigProperty("task-timeout")
	protected Integer taskTimeoutMillis;
	
	@XmlConfigProperty("task-hung-threshold")
	protected Integer taskHungThresholdMillis;
	
	@XmlConfigProperty("request-timeout")
	protected Integer requestTimeout;
	
	@ReflectionInjectedProperty("__m_AcceptorConfig")
	protected AcceptorConfig acceptorConfig;
	
	@ReflectionInjectedProperty("__m_RemoteCacheService")
	protected CacheServiceProxy remoteCacheService;
	
	@ReflectionInjectedProperty("__m_RemoteInvocationService")
	protected InvocationServiceProxy remoteInvocationService;
	
	@XmlConfigProperty("thread-count")
	protected Boolean autostart;
	
	@Override
	public ServiceType getServiceType() {
		return ServiceType.Proxy;
	}

	public Integer getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(Integer threadCount) {
		this.threadCount = threadCount;
	}

	public Integer getTaskTimeoutMillis() {
		return taskTimeoutMillis;
	}

	public void setTaskTimeoutMillis(Integer taskTimeoutMillis) {
		this.taskTimeoutMillis = taskTimeoutMillis;
	}

	public Integer getTaskHungThresholdMillis() {
		return taskHungThresholdMillis;
	}

	public void setTaskHungThresholdMillis(Integer taskHungThresholdMillis) {
		this.taskHungThresholdMillis = taskHungThresholdMillis;
	}

	public Integer getRequestTimeout() {
		return requestTimeout;
	}

	public void setRequestTimeout(Integer requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	public AcceptorConfig getAcceptorConfig() {
		return acceptorConfig;
	}

	public void setAcceptorConfig(AcceptorConfig acceptorConfig) {
		this.acceptorConfig = acceptorConfig;
	}

	public CacheServiceProxy getRemoteCacheService() {
		return remoteCacheService;
	}

	public void setRemoteCacheService(CacheServiceProxy remoteCacheService) {
		this.remoteCacheService = remoteCacheService;
	}

	public InvocationServiceProxy getRemoteInvocationService() {
		return remoteInvocationService;
	}

	public void setRemoteInvocationService(
			InvocationServiceProxy remoteInvocationService) {
		this.remoteInvocationService = remoteInvocationService;
	}

	public Boolean getAutostart() {
		return autostart;
	}

	public void setAutostart(Boolean autostart) {
		this.autostart = autostart;
	}
	
}
