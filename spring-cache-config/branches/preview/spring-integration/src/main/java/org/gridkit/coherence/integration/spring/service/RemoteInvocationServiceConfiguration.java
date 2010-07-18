package org.gridkit.coherence.integration.spring.service;

/**
 * @author Dmitri Babaev
 */
public class RemoteInvocationServiceConfiguration extends GenericServiceConfiguration {

	public ServiceType getServiceType() {
		return ServiceType.RemoteInvocation;
	}
}
