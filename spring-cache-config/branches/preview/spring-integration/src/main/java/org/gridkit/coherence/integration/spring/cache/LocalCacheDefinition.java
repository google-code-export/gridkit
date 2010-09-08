/**
 * Copyright 2010 Grid Dynamics Consulting Services, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gridkit.coherence.integration.spring.cache;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.gridkit.coherence.integration.spring.MapProvider;
import org.springframework.beans.factory.InitializingBean;

import com.tangosol.net.cache.LocalCache;
import com.tangosol.net.cache.ConfigurableCacheMap.EvictionPolicy;
import com.tangosol.net.cache.ConfigurableCacheMap.UnitCalculator;

/**
 * @author Dmitri Babaev
 */
@SuppressWarnings("deprecation")
public class LocalCacheDefinition implements MapProvider, InitializingBean {

	// TODO check defaults
	private int highUnits = 0;
	private int lowUnits = -1;
	private int expiryDelayMillis = 0;
	private int flushDelayMillis = -1;
	private EvictionPolicy evictionPolicy;
	private CacheEvictionType evictionType = CacheEvictionType.HYBRID;
	private UnitCalculator unitCalculator;
	private int unitFactor = -1;
		
	private LocalCache instance;

	private LocalCache createCache() {
		LocalCache cache = new LocalCache();
		if (highUnits > 0) {
			cache.setHighUnits(highUnits);
			if (lowUnits < 0) {
				lowUnits = (highUnits * 4) / 5;
			}
			cache.setLowUnits(lowUnits);
		}
		if (unitCalculator != null) {
			cache.setUnitCalculator(unitCalculator);
		}
		if (unitFactor > 0) {
			cache.setUnitFactor(unitFactor);
		}
		if (evictionPolicy != null)	{
			cache.setEvictionPolicy(evictionPolicy);
		}
		else {
			cache.setEvictionType(evictionType.type());
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
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (evictionPolicy == null && evictionType == null) {
			evictionType = CacheEvictionType.HYBRID;
		}
		instance = createCache();
	}
	
	public void setEvictionPolicy(EvictionPolicy evictionPolicy) {
		this.evictionPolicy = evictionPolicy;
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
	
	public void setUnitCalculator(UnitCalculator unitCalculator) {
		this.unitCalculator = unitCalculator;
	}

	public void setUnitFactor(int unitFactor) {
		this.unitFactor = unitFactor;
	}

	@Override
	public Map<?, ?> getMap() {
		return instance;
	}
}
