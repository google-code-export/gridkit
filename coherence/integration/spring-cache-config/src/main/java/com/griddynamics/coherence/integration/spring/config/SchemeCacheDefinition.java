package com.griddynamics.coherence.integration.spring.config;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Required;

import com.griddynamics.coherence.integration.spring.CacheFactory;
import com.tangosol.net.NamedCache;

/**
 * @author Dmitri Babaev
 */
public class SchemeCacheDefinition implements BeanNameAware, CacheDefinition {
	private CacheScheme cacheScheme;
	private CacheFactory cacheFactory;
	private String cacheName;
	
	public NamedCache newCache() {
		return cacheFactory.newCache(cacheName);
	}

	public void setBeanName(String name) {
		this.cacheName = name;
	}
	
	public CacheScheme getCacheScheme() {
		return cacheScheme;
	}
	
	@Required
	public void setCacheFactory(CacheFactory cacheFactory) {
		this.cacheFactory = cacheFactory;
	}
	
	@Required
	public void setCacheScheme(CacheScheme cacheScheme) {
		this.cacheScheme = cacheScheme;
	}
}
