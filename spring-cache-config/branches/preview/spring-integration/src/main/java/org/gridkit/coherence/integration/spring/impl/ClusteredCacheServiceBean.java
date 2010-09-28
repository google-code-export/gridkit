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

package org.gridkit.coherence.integration.spring.impl;

import java.util.Map;
import java.util.concurrent.Callable;

import org.gridkit.coherence.integration.spring.BackingMapLookupStrategy;
import org.springframework.beans.factory.annotation.Required;

import com.tangosol.net.AbstractBackingMapManager;
import com.tangosol.net.BackingMapManager;
import com.tangosol.net.CacheService;
import com.tangosol.net.Service;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class ClusteredCacheServiceBean extends CacheServiceBean {

	private BackingMapLookupStrategy backingMapLookupStrategy;
	private final BackingMapManager bmm = new BackendManager();
	
	@Required
	public void setBackingMapLookupStrategy(BackingMapLookupStrategy backingMapLookupStrategy) {
		this.backingMapLookupStrategy = backingMapLookupStrategy;
	}

	@Override
	public void destroy() throws Exception {
		// TODO destroying service
	}

	@Override
	protected void initializeService(Service service) {
		super.initializeService(service);
		((CacheService)service).setBackingMapManager(bmm);					
	}

	@Override
	protected void validateService(Service service) {
		super.validateService(service);
		if (((CacheService)service).getBackingMapManager() != bmm) {
			throw new IllegalArgumentException("Service name conflict. Service [" + serviceName + "] is owned by other service bean");
		}
	}

	private class BackendManager extends AbstractBackingMapManager {

		@Override
		@SuppressWarnings("unchecked")
		public Map instantiateBackingMap(final String cacheName) {
			Map backingMap = threadHelper.safeExecute(new Callable<Map>() {
				@Override
				public Map call() throws Exception {
					return backingMapLookupStrategy.instantiateBackingMap(cacheName, getContext());
				}				
			});
			jmxRegister(getContext().getCacheService(), backingMap, cacheName, "back");
			return backingMap;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void releaseBackingMap(String sName, Map map) {
			super.releaseBackingMap(sName, map);
			jmxUnregister(getContext().getCacheService(), sName, "back");
		}
	}
	
}
