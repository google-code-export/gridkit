package com.griddynamics.coherence.integration.spring.service;


/**
 * @author Dmitri Babaev
 */
public class OptimisticCacheServiceFactory extends CacheServiceFactory {
	
	public ServiceType getServiceType() {
		return ServiceType.OptimisticCache;
	}	
}
