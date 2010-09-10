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

import com.tangosol.net.AddressProvider;

/**
 * Fragment of Coherence*Extend configuration
 * @author malexejev@gmail.com
 * 09.09.2010
 */
public class TcpInitiatorConfig {

	@ReflectionInjectedProperty("__m_AddressProvider")
	private AddressProvider addressProvider;
	
	@XmlConfigProperty("local-host")
	private String localHost;
	
	@XmlConfigProperty("local-port")
	private Integer localPort;
	
	@XmlConfigProperty("socket-provider-config")
	private String socketProviderConfig;
	
	@XmlConfigProperty("reuse-address")
	private Boolean reuseAddress;
	
	@XmlConfigProperty("keep-alive-enabled")
	private Boolean keepAliveEnabled;
	
	@XmlConfigProperty("tcp-delay-enabled")
	private Boolean tcpDelayEnabled;
	
	@XmlConfigProperty("receive-buffer-size")
	private Integer receiveBufferSizeKBytes;
	
	@XmlConfigProperty("send-buffer-size")
	private Integer sendBufferSizeKBytes;
	
	@XmlConfigProperty("linger-timeout")
	private Integer lingerTimeoutMillis;
	
	@XmlConfigProperty("connect-timeout")
	private Integer connectTimeoutMillis;

	public AddressProvider getAddressProvider() {
		return addressProvider;
	}

	public void setAddressProvider(AddressProvider addressProvider) {
		this.addressProvider = addressProvider;
	}

	public String getLocalHost() {
		return localHost;
	}

	public void setLocalHost(String localHost) {
		this.localHost = localHost;
	}

	public Integer getLocalPort() {
		return localPort;
	}

	public void setLocalPort(Integer localPort) {
		this.localPort = localPort;
	}

	public String getSocketProviderConfig() {
		return socketProviderConfig;
	}

	public void setSocketProviderConfig(String socketProviderConfig) {
		this.socketProviderConfig = socketProviderConfig;
	}

	public Boolean getReuseAddress() {
		return reuseAddress;
	}

	public void setReuseAddress(Boolean reuseAddress) {
		this.reuseAddress = reuseAddress;
	}

	public Boolean getKeepAliveEnabled() {
		return keepAliveEnabled;
	}

	public void setKeepAliveEnabled(Boolean keepAliveEnabled) {
		this.keepAliveEnabled = keepAliveEnabled;
	}

	public Boolean getTcpDelayEnabled() {
		return tcpDelayEnabled;
	}

	public void setTcpDelayEnabled(Boolean tcpDelayEnabled) {
		this.tcpDelayEnabled = tcpDelayEnabled;
	}

	public Integer getReceiveBufferSizeKBytes() {
		return receiveBufferSizeKBytes;
	}

	public void setReceiveBufferSizeKBytes(Integer receiveBufferSizeKBytes) {
		this.receiveBufferSizeKBytes = receiveBufferSizeKBytes;
	}

	public Integer getSendBufferSizeKBytes() {
		return sendBufferSizeKBytes;
	}

	public void setSendBufferSizeKBytes(Integer sendBufferSizeKBytes) {
		this.sendBufferSizeKBytes = sendBufferSizeKBytes;
	}

	public Integer getLingerTimeoutMillis() {
		return lingerTimeoutMillis;
	}

	public void setLingerTimeoutMillis(Integer lingerTimeoutMillis) {
		this.lingerTimeoutMillis = lingerTimeoutMillis;
	}

	public Integer getConnectTimeoutMillis() {
		return connectTimeoutMillis;
	}

	public void setConnectTimeoutMillis(Integer connectTimeoutMillis) {
		this.connectTimeoutMillis = connectTimeoutMillis;
	}
	
}
