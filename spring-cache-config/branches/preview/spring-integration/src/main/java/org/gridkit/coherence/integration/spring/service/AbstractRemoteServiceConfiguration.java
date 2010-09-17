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

import java.util.List;

import com.tangosol.io.Serializer;
import com.tangosol.net.AddressProvider;
import com.tangosol.net.messaging.ConnectionFilter;
/**
 * Common configuration details for remote services,
 * such as caches and invocation
 * @author malexejev@gmail.com
 * 13.09.2010
 */
public abstract class AbstractRemoteServiceConfiguration extends
		AbstractServiceConfiguration {
	
	@XmlConfigProperty("initiator-config/outgoing-message-handler/heartbeat-interval")
	private Integer initiatorHeartbeatInterval;
	
	@XmlConfigProperty("initiator-config/outgoing-message-handler/heartbeat-timeout")
	private Integer initiatorHeartbeatTimeout;
	
	@XmlConfigProperty("initiator-config/outgoing-message-handler/request-timeout")
	private Integer initiatorRequestTimeout;
	
	// FIXME @ReflectionInjectedProperty("__m_Initiator.__m_Serializer")
	private Serializer initiatorSerializer;
	
	// TODO <initiator-config><use-filters>
	@ReflectionInjectedProperty("__m_Initiator.__m_ConnectionFilter")
	private ConnectionFilter initiatorConnectionFilter;
	
	@XmlConfigProperty("initiator-config/tcp-initiator/local-address/address")
	private String initiatorLocalHost;
	
	@XmlConfigProperty("initiator-config/tcp-initiator/local-address/port")
	private Integer initiatorLocalPort;
	
	@XmlConfigProperty("initiator-config/tcp-initiator/remote-addresses")
	private List<SocketAddressConfig> remoteAddresses;
	
	@ReflectionInjectedProperty("__m_Initiator.__m_AddressProvider")
	private AddressProvider addressProvider;
	
	// TODO add complex support for socket-provider configuration (now only works with string predefines)
	@XmlConfigProperty("initiator-config/tcp-initiator/socket-provider")
	private String initiatorSocketProviderConfig;
	
	@XmlConfigProperty("initiator-config/tcp-initiator/reuse-address")
	private Boolean initiatorReuseAddress;
	
	@XmlConfigProperty("initiator-config/tcp-initiator/keep-alive-enabled")
	private Boolean initiatorKeepAliveEnabled;
	
	@XmlConfigProperty("initiator-config/tcp-initiator/tcp-delay-enabled")
	private Boolean initiatorTcpDelayEnabled;
	
	@XmlConfigProperty("initiator-config/tcp-initiator/receive-buffer-size")
	private Integer initiatorReceiveBufferSize;
	
	@XmlConfigProperty("initiator-config/tcp-initiator/send-buffer-size")
	private Integer initiatorSendBufferSize;

	@XmlConfigProperty("initiator-config/tcp-initiator/linger-timeout")
	private Integer initiatorLingerTimeout;
	
	@XmlConfigProperty("initiator-config/tcp-initiator/connect-timeout")
	private Integer initiatorConnectTimeout;

	public Integer getInitiatorHeartbeatInterval() {
		return initiatorHeartbeatInterval;
	}

	public void setInitiatorHeartbeatInterval(Integer initiatorHeartbeatInterval) {
		this.initiatorHeartbeatInterval = initiatorHeartbeatInterval;
	}

	public Integer getInitiatorHeartbeatTimeout() {
		return initiatorHeartbeatTimeout;
	}

	public void setInitiatorHeartbeatTimeout(Integer initiatorHeartbeatTimeout) {
		this.initiatorHeartbeatTimeout = initiatorHeartbeatTimeout;
	}

	public Integer getInitiatorRequestTimeout() {
		return initiatorRequestTimeout;
	}

	public void setInitiatorRequestTimeout(Integer initiatorRequestTimeout) {
		this.initiatorRequestTimeout = initiatorRequestTimeout;
	}

	public Serializer getInitiatorSerializer() {
		return initiatorSerializer;
	}

	public void setInitiatorSerializer(Serializer initiatorSerializer) {
		this.initiatorSerializer = initiatorSerializer;
	}

	public ConnectionFilter getInitiatorConnectionFilter() {
		return initiatorConnectionFilter;
	}

	public void setInitiatorConnectionFilter(
			ConnectionFilter initiatorConnectionFilter) {
		this.initiatorConnectionFilter = initiatorConnectionFilter;
	}

	public String getInitiatorLocalHost() {
		return initiatorLocalHost;
	}

	public void setInitiatorLocalHost(String initiatorLocalHost) {
		this.initiatorLocalHost = initiatorLocalHost;
	}

	public Integer getInitiatorLocalPort() {
		return initiatorLocalPort;
	}

	public void setInitiatorLocalPort(Integer initiatorLocalPort) {
		this.initiatorLocalPort = initiatorLocalPort;
	}

	public AddressProvider getAddressProvider() {
		return addressProvider;
	}

	public void setAddressProvider(AddressProvider addressProvider) {
		this.addressProvider = addressProvider;
	}

	public String getInitiatorSocketProviderConfig() {
		return initiatorSocketProviderConfig;
	}

	public void setInitiatorSocketProviderConfig(
			String initiatorSocketProviderConfig) {
		this.initiatorSocketProviderConfig = initiatorSocketProviderConfig;
	}

	public Boolean getInitiatorReuseAddress() {
		return initiatorReuseAddress;
	}

	public void setInitiatorReuseAddress(Boolean initiatorReuseAddress) {
		this.initiatorReuseAddress = initiatorReuseAddress;
	}

	public Boolean getInitiatorKeepAliveEnabled() {
		return initiatorKeepAliveEnabled;
	}

	public void setInitiatorKeepAliveEnabled(Boolean initiatorKeepAliveEnabled) {
		this.initiatorKeepAliveEnabled = initiatorKeepAliveEnabled;
	}

	public Boolean getInitiatorTcpDelayEnabled() {
		return initiatorTcpDelayEnabled;
	}

	public void setInitiatorTcpDelayEnabled(Boolean initiatorTcpDelayEnabled) {
		this.initiatorTcpDelayEnabled = initiatorTcpDelayEnabled;
	}

	public Integer getInitiatorReceiveBufferSize() {
		return initiatorReceiveBufferSize;
	}

	public void setInitiatorReceiveBufferSize(Integer initiatorReceiveBufferSize) {
		this.initiatorReceiveBufferSize = initiatorReceiveBufferSize;
	}

	public Integer getInitiatorSendBufferSize() {
		return initiatorSendBufferSize;
	}

	public void setInitiatorSendBufferSize(Integer initiatorSendBufferSize) {
		this.initiatorSendBufferSize = initiatorSendBufferSize;
	}

	public Integer getInitiatorLingerTimeout() {
		return initiatorLingerTimeout;
	}

	public void setInitiatorLingerTimeout(Integer initiatorLingerTimeout) {
		this.initiatorLingerTimeout = initiatorLingerTimeout;
	}

	public Integer getInitiatorConnectTimeout() {
		return initiatorConnectTimeout;
	}

	public void setInitiatorConnectTimeout(Integer initiatorConnectTimeout) {
		this.initiatorConnectTimeout = initiatorConnectTimeout;
	}

	public List<SocketAddressConfig> getRemoteAddresses() {
		return remoteAddresses;
	}

	public void setRemoteAddresses(List<SocketAddressConfig> remoteAddresses) {
		this.remoteAddresses = remoteAddresses;
	}

}
