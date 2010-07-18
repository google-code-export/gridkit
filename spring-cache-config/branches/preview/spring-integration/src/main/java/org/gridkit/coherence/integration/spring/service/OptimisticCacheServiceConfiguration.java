package org.gridkit.coherence.integration.spring.service;


/**
 * @author Dmitri Babaev
 */
public class OptimisticCacheServiceConfiguration extends ReplicatedCacheServiceConfiguration {
	
	public ServiceType getServiceType() {
		return ServiceType.OptimisticCache;
	}	
}
