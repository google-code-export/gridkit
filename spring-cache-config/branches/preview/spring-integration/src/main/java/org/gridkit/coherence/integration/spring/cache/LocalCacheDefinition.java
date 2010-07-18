package org.gridkit.coherence.integration.spring.cache;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.gridkit.coherence.integration.spring.MapProvider;
import org.springframework.beans.factory.InitializingBean;

import com.tangosol.net.cache.LocalCache;
import com.tangosol.net.cache.ConfigurableCacheMap.UnitCalculator;

/**
 * @author Dmitri Babaev
 */
public class LocalCacheDefinition implements MapProvider, InitializingBean {

	// TODO check defaults
	private int highUnits = 0;
	private int lowUnits = -1;
	private int expiryDelayMillis = 0;
	private int flushDelayMillis = -1;
	private CacheEvictionType evictionType = CacheEvictionType.HYBRID;
	private UnitCalculator unitCalculator;
		
	private LocalCache instance;

	@SuppressWarnings("deprecation")
	private LocalCache createCache() {
		LocalCache cache = new LocalCache();
		if (highUnits > 0) {
			cache.setHighUnits(highUnits);
			if (lowUnits < 0) {
				lowUnits = (highUnits * 4) / 5;
			}
			cache.setLowUnits(lowUnits);
			cache.setEvictionType(evictionType.type());
			if (unitCalculator != null) {
				cache.setUnitCalculator(unitCalculator);
			}
		}
		if (expiryDelayMillis > 0) {
			cache.setExpiryDelay(expiryDelayMillis);
			if (flushDelayMillis < 0) {
				flushDelayMillis = (int) TimeUnit.MINUTES.toMillis(1);
			}
			cache.setFlushDelay(flushDelayMillis);
		}
		
		return cache;
	}

	public Class<LocalCache> getObjectType() {
		return LocalCache.class;
	}

	public boolean isSingleton() {
		return false;
	}
	
	public void afterPropertiesSet() throws Exception {
		instance = createCache();
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
	
	@Override
	public Map<?, ?> getMap() {
		return instance;
	}
}
