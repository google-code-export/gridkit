package com.griddynamics.coherence.integration.spring.config;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Dmitri Babaev
 */
public abstract class CacheScheme implements BeanNameAware {
	private String serviceName;
	
	public void setBeanName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	public String getServiceName() {
		return serviceName;
	}
}
