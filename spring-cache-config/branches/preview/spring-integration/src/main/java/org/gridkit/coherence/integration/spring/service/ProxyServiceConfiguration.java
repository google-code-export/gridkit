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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.gridkit.coherence.integration.spring.CacheLookupStrategy;

import com.tangosol.coherence.component.net.extend.proxy.serviceProxy.CacheServiceProxy;
import com.tangosol.coherence.component.net.extend.proxy.serviceProxy.InvocationServiceProxy;
import com.tangosol.coherence.component.util.daemon.queueProcessor.service.grid.ProxyService;
import com.tangosol.io.Serializer;
import com.tangosol.net.AddressProvider;
import com.tangosol.net.Service;
import com.tangosol.net.messaging.ConnectionFilter;
import com.tangosol.util.Filter;


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
	protected Integer taskTimeout;
	
	@XmlConfigProperty("task-hung-threshold")
	protected Integer taskHungThreshold;
	
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
	
	@ReflectionInjectedProperty("__m_Acceptor.__m_Serializer")
	private Serializer acceptorSerializer;
	
	// FIXME
	@XmlConfigProperty("acceptor-config/serializer/class-name") /* instance/ */
	private String acceptorSerializerClass;
	
	public String getAcceptorSerializerClass() {
		return acceptorSerializerClass;
	}

	public void setAcceptorSerializerClass(String acceptorSerializerClass) {
		this.acceptorSerializerClass = acceptorSerializerClass;
	}

	@XmlConfigProperty("acceptor-config/tcp-acceptor/authorized-hosts/host-address")
	private String authorizedHostAddress;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/authorized-hosts/host-range")
	private String authorizedHostRange;

	@ReflectionInjectedProperty("__m_Acceptor.__m_AuthorizedHostFilter")
	private Filter authorizedHostFilter;
	
	@ReflectionInjectedProperty("__m_Acceptor.__m_LocalAddressProvider")
	private AddressProvider addressProvider;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/local-address/address")
	private String acceptorLocalHost;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/local-address/port")
	private Integer acceptorLocalPort;
	
	// TODO add complex support for socket-provider configuration (now only works with string predefines)
	@XmlConfigProperty("acceptor-config/tcp-acceptor/socket-provider")
	private String acceptorSocketProviderConfig;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/reuse-address")
	private Boolean acceptorReuseAddress;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/keep-alive-enabled")
	private Boolean acceptorKeepAliveEnabled;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/tcp-delay-enabled")
	private Boolean acceptorTcpDelayEnabled;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/receive-buffer-size")
	private Integer acceptorReceiveBufferSize;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/send-buffer-size")
	private Integer acceptorSendBufferSize;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/linger-timeout")
	private Integer acceptorLingerTimeout;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/listen-backlog")
	private Integer acceptorListenBacklog;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/suspect-protocol-enabled")
	private Boolean acceptorSuspectProtocolEnabled;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/suspect-buffer-size")
	private Integer acceptorSuspectBufferSize;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/suspect-buffer-length")
	private Integer acceptorSuspectBufferLength;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/nominal-buffer-size")
	private Integer acceptorNominalBufferSize;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/nominal-buffer-length")
	private Integer acceptorNominalBufferLength;
	
	@XmlConfigProperty("acceptor-config/tcp-acceptor/limit-buffer-size")
	private Integer acceptorLimitBufferSize;
	
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
	
	private CacheLookupStrategy cacheLookupStrategy;
	
	// see public void postConfigure(Service service)
	@SuppressWarnings("unused")
	@ReflectionInjectedProperty("__m_InvocationServiceProxy")
	private InvocationServiceProxy invocationServiceProxyWrapper;
	
	@Override
	public ServiceType getServiceType() {
		return ServiceType.Proxy;
	}
	
	@Override
	protected void overrideInstanceFields(Service service) 
			throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, 
			InvocationTargetException, NoSuchFieldException {
		
		super.overrideInstanceFields(service);
		
		Field cacheServiceProxyField = ProxyService.class.getDeclaredField("__m_CacheServiceProxy");
		cacheServiceProxyField.setAccessible(true);
		CacheServiceProxy cacheProxy = (CacheServiceProxy) cacheServiceProxyField.get(service);
		
		Field cacheServiceField = CacheServiceProxy.class.getDeclaredField("__m_CacheService");
		cacheServiceField.setAccessible(true);
		cacheServiceField.set(cacheProxy, new CacheServiceProxyWrapper(cacheProxy, cacheLookupStrategy));
	}
	
	public Integer getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(Integer threadCount) {
		this.threadCount = threadCount;
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

	public Filter getAuthorizedHostFilter() {
		return authorizedHostFilter;
	}

	public void setAuthorizedHostFilter(Filter authorizedHostFilter) {
		this.authorizedHostFilter = authorizedHostFilter;
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

	public Integer getAcceptorSuspectBufferLength() {
		return acceptorSuspectBufferLength;
	}

	public void setAcceptorSuspectBufferLength(Integer acceptorSuspectBufferLength) {
		this.acceptorSuspectBufferLength = acceptorSuspectBufferLength;
	}

	public Integer getAcceptorNominalBufferLength() {
		return acceptorNominalBufferLength;
	}

	public void setAcceptorNominalBufferLength(Integer acceptorNominalBufferLength) {
		this.acceptorNominalBufferLength = acceptorNominalBufferLength;
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

	public Integer getTaskTimeout() {
		return taskTimeout;
	}

	public void setTaskTimeout(Integer taskTimeout) {
		this.taskTimeout = taskTimeout;
	}

	public Integer getTaskHungThreshold() {
		return taskHungThreshold;
	}

	public void setTaskHungThreshold(Integer taskHungThreshold) {
		this.taskHungThreshold = taskHungThreshold;
	}

	public Integer getAcceptorReceiveBufferSize() {
		return acceptorReceiveBufferSize;
	}

	public void setAcceptorReceiveBufferSize(Integer acceptorReceiveBufferSize) {
		this.acceptorReceiveBufferSize = acceptorReceiveBufferSize;
	}

	public Integer getAcceptorSendBufferSize() {
		return acceptorSendBufferSize;
	}

	public void setAcceptorSendBufferSize(Integer acceptorSendBufferSize) {
		this.acceptorSendBufferSize = acceptorSendBufferSize;
	}

	public Integer getAcceptorLingerTimeout() {
		return acceptorLingerTimeout;
	}

	public void setAcceptorLingerTimeout(Integer acceptorLingerTimeout) {
		this.acceptorLingerTimeout = acceptorLingerTimeout;
	}

	public Integer getAcceptorSuspectBufferSize() {
		return acceptorSuspectBufferSize;
	}

	public void setAcceptorSuspectBufferSize(Integer acceptorSuspectBufferSize) {
		this.acceptorSuspectBufferSize = acceptorSuspectBufferSize;
	}

	public Integer getAcceptorNominalBufferSize() {
		return acceptorNominalBufferSize;
	}

	public void setAcceptorNominalBufferSize(Integer acceptorNominalBufferSize) {
		this.acceptorNominalBufferSize = acceptorNominalBufferSize;
	}

	public Integer getAcceptorLimitBufferSize() {
		return acceptorLimitBufferSize;
	}

	public void setAcceptorLimitBufferSize(Integer acceptorLimitBufferSize) {
		this.acceptorLimitBufferSize = acceptorLimitBufferSize;
	}

	public CacheLookupStrategy getCacheLookupStrategy() {
		return cacheLookupStrategy;
	}

	public void setCacheLookupStrategy(CacheLookupStrategy cacheLookupStrategy) {
		this.cacheLookupStrategy = cacheLookupStrategy;
	}
	
}
