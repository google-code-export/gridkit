package org.gridkit.coherence.integration.spring.service;

/**
 * @author Dmitri Babaev
 */
public class ProxyServiceConfiguration extends GenericServiceConfiguration {

	public ServiceType getServiceType() {
		return ServiceType.Proxy;
	}
}
