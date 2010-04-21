package com.griddynamics.coherence.integration.spring.config;

import org.springframework.beans.factory.FactoryBean;

import com.griddynamics.coherence.integration.spring.CacheFactory;
import com.tangosol.net.NamedCache;

/**
 * @author Dmitri Babaev
 */
public class NamedCacheFactory implements FactoryBean<NamedCache> {
	private String cacheName;
	private CacheFactory cacheFactory;

	public NamedCache getObject() throws Exception {
		return cacheFactory.newCache(cacheName);
	}
	
	public Class<?> getObjectType() {
		return NamedCache.class;
	}
	
	public boolean isSingleton() {
		return true;
	}
	
	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}
}
