package com.griddynamics.coherence.integration.spring.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

import com.griddynamics.coherence.integration.spring.LazyNamedCache;
import com.tangosol.net.NamedCache;

/**
 * @author Dmitri Babaev
 */
public class NamedCacheFactory implements FactoryBean<NamedCache> {
	private CacheDefinition cacheDefinition;

	public NamedCache getObject() throws Exception {
		return new LazyNamedCache(cacheDefinition);
	}
	
	public Class<?> getObjectType() {
		return NamedCache.class;
	}
	
	public boolean isSingleton() {
		return false;
	}
	
	@Required
	public void setCacheDefinition(CacheDefinition cacheDefinition) {
		this.cacheDefinition = cacheDefinition;
	}
}
