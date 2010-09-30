package com.griddynamics.coherence.integration.spring.service;

/**
 * @author Dmitri Babaev
 */
public class RemoteCacheServiceFactory extends ServiceFactory {
	
	public ServiceType getServiceType() {
		return ServiceType.RemoteCache;
	}
}
