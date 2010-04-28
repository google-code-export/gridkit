package com.griddynamics.coherence.integration.spring;

import java.util.Map;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.NearCache;

/**
 * @author Dmitri Babaev
 */
public class NearCacheFactory implements FactoryBean<NamedCache> {
	private CacheDefinition backingCacheDefinition;
	private Map<?, ?> frontMap;
	private InvalidationStrategy invalidationStrategy = InvalidationStrategy.auto;
	
	@Required
	public void setBackingCacheDefinition(CacheDefinition backingCacheDefinition) {
		this.backingCacheDefinition = backingCacheDefinition;
	}
	
	@Required
	public void setFrontMap(Map<?, ?> frontMap) {
		this.frontMap = frontMap;
	}
	
	public void setInvalidationStrategy(
			InvalidationStrategy invalidationStrategy) {
		this.invalidationStrategy = invalidationStrategy;
	}

	public NamedCache getObject() throws Exception {
		NamedCache backingCache = backingCacheDefinition.newCache();
		NearCache res = new NearCache(frontMap, backingCache, invalidationStrategy.type());
		return res;
	}

	public Class<NamedCache> getObjectType() {
		return NamedCache.class;
	}

	public boolean isSingleton() {
		return false;
	}
}
