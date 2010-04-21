package com.griddynamics.coherence.integration.spring.config;

import org.springframework.beans.factory.annotation.Required;

/**
 * @author Dmitri Babaev
 */
public abstract class CacheScheme {
	private String serviceName;
	
	@Required
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	public String getServiceName() {
		return serviceName;
	}
}
