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

import org.gridkit.coherence.integration.spring.BackningMapProvider;
import org.springframework.beans.factory.InitializingBean;

import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.cache.CacheLoader;
import com.tangosol.net.cache.ReadWriteBackingMap;
import com.tangosol.util.ObservableMap;

/**
 * @author Dmitri Babaev
 */
public class ReadWriteBackingMapDefinition implements BackningMapProvider, InitializingBean {
	
	private Object internalMap;
	private Object missesMap;
	private CacheLoader cachestore;
	private boolean readOnly = false;
	private Integer cacheStoreTimeoutMillis;
	private int writeBehindSeconds = 0;
	private double refreshAheadFactor = 0.0D;
	private Double writeBatchFactor;
	private Integer writeRequeueThreshold;
	
	private ReadWriteBackingMap instance;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO validation
	}

	@Override
	public Map<?, ?> getBackinMap(BackingMapManagerContext context) {
		ensureBackingMap(context);
		return instance;
	}

	private synchronized void ensureBackingMap(BackingMapManagerContext cacheCtx) {
		ObservableMap internal = BackningMapProvider.Helper.getMapFromBean(internalMap, cacheCtx, ObservableMap.class);
		Map<?, ?> misses = missesMap == null ? null : BackningMapProvider.Helper.getMapFromBean(missesMap, cacheCtx, Map.class);
		
		instance = new ReadWriteBackingMap(cacheCtx, internal, misses, cachestore, readOnly, writeBehindSeconds, refreshAheadFactor);
		if (cacheStoreTimeoutMillis != null) {
			instance.setCacheStoreTimeoutMillis(cacheStoreTimeoutMillis);
		}
		if (writeBatchFactor != null) {
			instance.setWriteBatchFactor(writeBatchFactor);
		}
		if (writeRequeueThreshold != null) {
			instance.setWriteRequeueThreshold(writeRequeueThreshold);
		}
	}
	
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	public void setCacheStoreTimeoutMillis(int cacheStoreTimeout) {
		this.cacheStoreTimeoutMillis = cacheStoreTimeout;
	}

	public void setLoader(CacheLoader loader) {
		this.cachestore = loader;
	}

	public void setCachestore(CacheLoader store) {
		this.cachestore = store;
	}
	
	public void setRefreshAheadFactor(double refreshAheadFactor) {
		this.refreshAheadFactor = refreshAheadFactor;
	}
	
	public void setInternalMap(Object internalMap) {
		this.internalMap = internalMap;
	}
	
	public void setMissesMap(Object missesMap) {
		this.missesMap = missesMap;
	}
	
	public void setWriteBehindSeconds(int writeBehindSeconds) {
		this.writeBehindSeconds = writeBehindSeconds;
	}

	public void setWriteBatchFactor(double writeBatchFactor) {
		this.writeBatchFactor = writeBatchFactor;
	}

	public void setWriteRequeueThreshold(int writeRequeueThreshold) {
		this.writeRequeueThreshold = writeRequeueThreshold;
	}
	
	
}
