package com.griddynamics.coherence.integration.spring.config;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Required;

import com.tangosol.net.CacheService;
import com.tangosol.net.NamedCache;

/**
 * @author Dmitri Babaev
 */
public class ServiceCacheDefinition implements BeanNameAware {
	private String cacheName;
	private CacheService service;
	
	public NamedCache newCache() {
		return service.ensureCache(cacheName, getContextClassLoader());
	}

	private ClassLoader getContextClassLoader() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setBeanName(String name) {
		this.cacheName = name;
	}
	
	@Required
	public void setCacheService(CacheService service) {
		this.service = service;
	}
}
