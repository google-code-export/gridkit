package com.griddynamics.coherence.integration.spring.config;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

import com.griddynamics.coherence.integration.spring.CacheFactory;
import com.tangosol.net.NamedCache;

/**
 * @author Dmitri Babaev
 */
public class NamedCacheFactory implements FactoryBean<NamedCache>, BeanNameAware {
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
	
	public void setBeanName(String cacheName) {
		this.cacheName = cacheName;
	}
	
	@Required
	public void setCacheFactory(CacheFactory cacheFactory) {
		this.cacheFactory = cacheFactory;
	}
}
