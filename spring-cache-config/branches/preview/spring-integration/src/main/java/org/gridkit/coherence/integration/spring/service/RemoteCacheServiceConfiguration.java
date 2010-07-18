package org.gridkit.coherence.integration.spring.service;

/**
 * @author Dmitri Babaev
 */
public class RemoteCacheServiceConfiguration extends GenericServiceConfiguration {
	
	public ServiceType getServiceType() {
		return ServiceType.RemoteCache;
	}
}
