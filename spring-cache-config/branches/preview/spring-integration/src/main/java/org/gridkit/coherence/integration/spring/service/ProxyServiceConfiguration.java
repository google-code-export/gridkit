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

import com.tangosol.io.Serializer;
import com.tangosol.net.AddressProvider;
import com.tangosol.net.messaging.ConnectionFilter;


/**
 * Fragment of Coherence*Extend configuration
 * com.tangosol.coherence.component.util.daemon.queueProcessor.service.grid.ProxyService
 * 
 * @author Max Alexejev (malexejev@gmail.com)
 * 06.09.2010
 */
public class ProxyServiceConfiguration extends AbstractServiceConfiguration {
	
	@XmlConfigProperty("thread-count")
	protected Integer threadCount;
	
	@XmlConfigProperty("task-timeout")
	protected Integer taskTimeoutMillis;
	
	@XmlConfigProperty("task-hung-threshold")
	protected Integer taskHungThresholdMillis;
	
	@XmlConfigProperty("request-timeout")
	protected Integer requestTimeout;
	
	@XmlConfigProperty("autostart")
	protected Boolean autostart;
	
	@ReflectionInjectedProperty("__m_Acceptor.__m_ConnectionLimit")
	private Integer connectionLimit;
	
	@XmlConfigProperty("acceptor-config/outgoing-message-handler/heartbeat-interval")
	private Integer acceptorHeartbeatInterval;
	
	@XmlConfigProperty("acceptor-config/outgoing-message-handler/heartbeat-timeout")
	private Integer acceptorHeartbeatTimeout;
	
	@XmlConfigProperty("acceptor-config/outgoing-message-handler/request-timeout")
	private Integer acceptorRequestTimeout;
	
	// FIXME @ReflectionInjectedProperty("__m_Acceptor.__m_Serializer")
	private Serializer acceptorSerializer;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/authorized-hosts/host-address")
	private String authorizedHostAddress;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/authorized-hosts/host-range")
	private String authorizedHostRange;

	@ReflectionInjectedProperty("__m_Acceptor.__m_AddressProvider")
	private AddressProvider addressProvider;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/local-address/address")
	private String acceptorLocalHost;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/local-address/port")
	private Integer acceptorLocalPort;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/socket-provider-config")
	private String acceptorSocketProviderConfig;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/reuse-address")
	private Boolean acceptorReuseAddress;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/keep-alive-enabled")
	private Boolean acceptorKeepAliveEnabled;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/tcp-delay-enabled")
	private Boolean acceptorTcpDelayEnabled;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/receive-buffer-size")
	private Integer acceptorReceiveBufferSizeBytes;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/send-buffer-size")
	private Integer acceptorSendBufferSizeBytes;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/linger-timeout")
	private Integer acceptorLingerTimeoutMillis;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/listen-backlog")
	private Integer acceptorListenBacklog;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/suspect-protocol-enabled")
	private Boolean acceptorSuspectProtocolEnabled;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/suspect-buffer-size")
	private Integer acceptorSuspectBufferSizeBytes;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/suspect-buffer-length")
	private Integer acceptorSuspectBufferLength;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/nominal-buffer-size")
	private Integer acceptorNominalBufferSizeBytes;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/nominal-buffer-length")
	private Integer acceptorNominalBufferLength;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/limit-buffer-size")
	private Integer acceptorLimitBufferSizeBytes;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/limit-buffer-length")
	private Integer acceptorLimitBufferLength;
	
	@ReflectionInjectedProperty("__m_Acceptor.__m_ConnectionFilter")
	private ConnectionFilter acceptorConnectionFilter;
	
	@XmlConfigProperty("proxy-config/cache-service-proxy/enabled")
	private Boolean cacheProxyEnabled;
	
	@XmlConfigProperty("proxy-config/cache-service-proxy/lock-enabled")
	private Boolean cacheProxyLockEnabled;
	
	@XmlConfigProperty("proxy-config/cache-service-proxy/read-only")
	private Boolean cacheProxyReadOnly;
	
	@XmlConfigProperty("proxy-config/cache-service-proxy/class-name")
	private String cacheProxyClassName;
	
	@XmlConfigProperty("proxy-config/invocation-service-proxy/enabled")
	private Boolean invocationProxyEnabled;
	
	@XmlConfigProperty("proxy-config/invocation-service-proxy/class-name")
	private String invocationProxyClassName;
	
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

	public Boolean getAutostart() {
		return autostart;
	}

	public void setAutostart(Boolean autostart) {
		this.autostart = autostart;
	}

	public Integer getConnectionLimit() {
		return connectionLimit;
	}

	public void setConnectionLimit(Integer connectionLimit) {
		this.connectionLimit = connectionLimit;
	}

	public Integer getAcceptorHeartbeatInterval() {
		return acceptorHeartbeatInterval;
	}

	public void setAcceptorHeartbeatInterval(Integer acceptorHeartbeatInterval) {
		this.acceptorHeartbeatInterval = acceptorHeartbeatInterval;
	}

	public Integer getAcceptorHeartbeatTimeout() {
		return acceptorHeartbeatTimeout;
	}

	public void setAcceptorHeartbeatTimeout(Integer acceptorHeartbeatTimeout) {
		this.acceptorHeartbeatTimeout = acceptorHeartbeatTimeout;
	}

	public Integer getAcceptorRequestTimeout() {
		return acceptorRequestTimeout;
	}

	public void setAcceptorRequestTimeout(Integer acceptorRequestTimeout) {
		this.acceptorRequestTimeout = acceptorRequestTimeout;
	}

	public String getAuthorizedHostAddress() {
		return authorizedHostAddress;
	}

	public void setAuthorizedHostAddress(String authorizedHostAddress) {
		this.authorizedHostAddress = authorizedHostAddress;
	}

	public String getAuthorizedHostRange() {
		return authorizedHostRange;
	}

	public void setAuthorizedHostRange(String authorizedHostRange) {
		this.authorizedHostRange = authorizedHostRange;
	}

	public AddressProvider getAddressProvider() {
		return addressProvider;
	}

	public void setAddressProvider(AddressProvider addressProvider) {
		this.addressProvider = addressProvider;
	}

	public String getAcceptorLocalHost() {
		return acceptorLocalHost;
	}

	public void setAcceptorLocalHost(String acceptorLocalHost) {
		this.acceptorLocalHost = acceptorLocalHost;
	}

	public Integer getAcceptorLocalPort() {
		return acceptorLocalPort;
	}

	public void setAcceptorLocalPort(Integer acceptorLocalPort) {
		this.acceptorLocalPort = acceptorLocalPort;
	}

	public String getAcceptorSocketProviderConfig() {
		return acceptorSocketProviderConfig;
	}

	public void setAcceptorSocketProviderConfig(String acceptorSocketProviderConfig) {
		this.acceptorSocketProviderConfig = acceptorSocketProviderConfig;
	}

	public Boolean getAcceptorReuseAddress() {
		return acceptorReuseAddress;
	}

	public void setAcceptorReuseAddress(Boolean acceptorReuseAddress) {
		this.acceptorReuseAddress = acceptorReuseAddress;
	}

	public Boolean getAcceptorKeepAliveEnabled() {
		return acceptorKeepAliveEnabled;
	}

	public void setAcceptorKeepAliveEnabled(Boolean acceptorKeepAliveEnabled) {
		this.acceptorKeepAliveEnabled = acceptorKeepAliveEnabled;
	}

	public Boolean getAcceptorTcpDelayEnabled() {
		return acceptorTcpDelayEnabled;
	}

	public void setAcceptorTcpDelayEnabled(Boolean acceptorTcpDelayEnabled) {
		this.acceptorTcpDelayEnabled = acceptorTcpDelayEnabled;
	}

	public Serializer getAcceptorSerializer() {
		return acceptorSerializer;
	}

	public void setAcceptorSerializer(Serializer acceptorSerializer) {
		this.acceptorSerializer = acceptorSerializer;
	}

	public Integer getAcceptorReceiveBufferSizeBytes() {
		return acceptorReceiveBufferSizeBytes;
	}

	public void setAcceptorReceiveBufferSizeBytes(
			Integer acceptorReceiveBufferSizeBytes) {
		this.acceptorReceiveBufferSizeBytes = acceptorReceiveBufferSizeBytes;
	}

	public Integer getAcceptorSendBufferSizeBytes() {
		return acceptorSendBufferSizeBytes;
	}

	public void setAcceptorSendBufferSizeBytes(Integer acceptorSendBufferSizeBytes) {
		this.acceptorSendBufferSizeBytes = acceptorSendBufferSizeBytes;
	}

	public Integer getAcceptorLingerTimeoutMillis() {
		return acceptorLingerTimeoutMillis;
	}

	public void setAcceptorLingerTimeoutMillis(Integer acceptorLingerTimeoutMillis) {
		this.acceptorLingerTimeoutMillis = acceptorLingerTimeoutMillis;
	}

	public Integer getAcceptorListenBacklog() {
		return acceptorListenBacklog;
	}

	public void setAcceptorListenBacklog(Integer acceptorListenBacklog) {
		this.acceptorListenBacklog = acceptorListenBacklog;
	}

	public Boolean getAcceptorSuspectProtocolEnabled() {
		return acceptorSuspectProtocolEnabled;
	}

	public void setAcceptorSuspectProtocolEnabled(
			Boolean acceptorSuspectProtocolEnabled) {
		this.acceptorSuspectProtocolEnabled = acceptorSuspectProtocolEnabled;
	}

	public Integer getAcceptorSuspectBufferSizeBytes() {
		return acceptorSuspectBufferSizeBytes;
	}

	public void setAcceptorSuspectBufferSizeBytes(
			Integer acceptorSuspectBufferSizeBytes) {
		this.acceptorSuspectBufferSizeBytes = acceptorSuspectBufferSizeBytes;
	}

	public Integer getAcceptorSuspectBufferLength() {
		return acceptorSuspectBufferLength;
	}

	public void setAcceptorSuspectBufferLength(Integer acceptorSuspectBufferLength) {
		this.acceptorSuspectBufferLength = acceptorSuspectBufferLength;
	}

	public Integer getAcceptorNominalBufferSizeBytes() {
		return acceptorNominalBufferSizeBytes;
	}

	public void setAcceptorNominalBufferSizeBytes(
			Integer acceptorNominalBufferSizeBytes) {
		this.acceptorNominalBufferSizeBytes = acceptorNominalBufferSizeBytes;
	}

	public Integer getAcceptorNominalBufferLength() {
		return acceptorNominalBufferLength;
	}

	public void setAcceptorNominalBufferLength(Integer acceptorNominalBufferLength) {
		this.acceptorNominalBufferLength = acceptorNominalBufferLength;
	}

	public Integer getAcceptorLimitBufferSizeBytes() {
		return acceptorLimitBufferSizeBytes;
	}

	public void setAcceptorLimitBufferSizeBytes(Integer acceptorLimitBufferSizeBytes) {
		this.acceptorLimitBufferSizeBytes = acceptorLimitBufferSizeBytes;
	}

	public Integer getAcceptorLimitBufferLength() {
		return acceptorLimitBufferLength;
	}

	public void setAcceptorLimitBufferLength(Integer acceptorLimitBufferLength) {
		this.acceptorLimitBufferLength = acceptorLimitBufferLength;
	}

	public ConnectionFilter getAcceptorConnectionFilter() {
		return acceptorConnectionFilter;
	}

	public void setAcceptorConnectionFilter(
			ConnectionFilter acceptorConnectionFilter) {
		this.acceptorConnectionFilter = acceptorConnectionFilter;
	}

	public Boolean getCacheProxyEnabled() {
		return cacheProxyEnabled;
	}

	public void setCacheProxyEnabled(Boolean cacheProxyEnabled) {
		this.cacheProxyEnabled = cacheProxyEnabled;
	}

	public Boolean getCacheProxyLockEnabled() {
		return cacheProxyLockEnabled;
	}

	public void setCacheProxyLockEnabled(Boolean cacheProxyLockEnabled) {
		this.cacheProxyLockEnabled = cacheProxyLockEnabled;
	}

	public Boolean getCacheProxyReadOnly() {
		return cacheProxyReadOnly;
	}

	public void setCacheProxyReadOnly(Boolean cacheProxyReadOnly) {
		this.cacheProxyReadOnly = cacheProxyReadOnly;
	}

	public String getCacheProxyClassName() {
		return cacheProxyClassName;
	}

	public void setCacheProxyClassName(String cacheProxyClassName) {
		this.cacheProxyClassName = cacheProxyClassName;
	}

	public Boolean getInvocationProxyEnabled() {
		return invocationProxyEnabled;
	}

	public void setInvocationProxyEnabled(Boolean invocationProxyEnabled) {
		this.invocationProxyEnabled = invocationProxyEnabled;
	}

	public String getInvocationProxyClassName() {
		return invocationProxyClassName;
	}

	public void setInvocationProxyClassName(String invocationProxyClassName) {
		this.invocationProxyClassName = invocationProxyClassName;
	}
	
}
