package com.griddynamics.coherence.integration.spring.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.tangosol.net.cache.LocalCache;

/**
 * @author Dmitri Babaev
 */
public class LocalCacheFactory implements FactoryBean<LocalCache>, InitializingBean {
	int highUnits = 2147483647;
	int lowUnits = (int) (highUnits * 0.8D);
	int expiryDelayMillis = 1000;
	int flushDelayMillis = 1000;
	private CacheEvictionType evictionType = CacheEvictionType.HYBRID; 

	@SuppressWarnings("deprecation")
	public LocalCache getObject() throws Exception {
		LocalCache cache = new LocalCache(highUnits, expiryDelayMillis);
		cache.setFlushDelay(flushDelayMillis);
		cache.setLowUnits(lowUnits);
		cache.setEvictionType(evictionType.type());
		
		return cache;
	}

	public Class<LocalCache> getObjectType() {
		return LocalCache.class;
	}

	public boolean isSingleton() {
		return false;
	}
	
	public void afterPropertiesSet() throws Exception {
		lowUnits = (int) (highUnits * 0.8D);
		
		if ((expiryDelayMillis > 0) && (flushDelayMillis == 0)) {
			flushDelayMillis = 60000;
		}
	}
	
	public void setEvictionType(CacheEvictionType evictionType) {
		this.evictionType = evictionType;
	}
	
	public void setExpiryDelayMillis(int expiryDelayMillis) {
		this.expiryDelayMillis = expiryDelayMillis;
	}
	
	public void setFlushDelayMillis(int flushDelayMillis) {
		this.flushDelayMillis = flushDelayMillis;
	}
	
	public void setHighUnits(int highUnits) {
		this.highUnits = highUnits;
	}
	
	public void setLowUnits(int lowUnits) {
		this.lowUnits = lowUnits;
	}
}
