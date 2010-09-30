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

import org.gridkit.coherence.integration.spring.ClusteredCacheService;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.tangosol.net.CacheService;
import com.tangosol.net.NamedCache;
import com.tangosol.net.management.Registry;

/**
 * Common logic for both local and remote caches
 * @author malexejev@gmail.com
 * 14.09.2010
 */
public class CacheServiceBean extends ClusteredServiceBean implements ClusteredCacheService, InitializingBean, BeanNameAware, DisposableBean {

	@Override
	public NamedCache ensureCache(final String name) {
		ensureStarted();
		return threadHelper.modalExecute(new Callable<NamedCache>() {
			@Override
			public NamedCache call() throws Exception {
				return getCacheService().ensureCache(name, null);
			}
		});
	}
	
	@Override
	public void destroyCache(NamedCache cache) {
		getCacheService().destroyCache(cache);
	}
	
	protected CacheService getCacheService() {
		return ((CacheService)service);
	}
	
	protected void jmxRegister(CacheService cacheService, Map<?, ?> cache, String name, String tier) {
		Registry r = cacheService.getCluster().getManagement();
		String id = "type=Cache,serive=" + cacheService.getInfo().getServiceName() + ",name=" + name + ",tier=" + tier;
		id = r.ensureGlobalName(id);
		r.register(id, cache);		
	}
	
	protected void jmxUnregister(CacheService cacheService, String name, String tier) {
		Registry r = cacheService.getCluster().getManagement();
		String id = "type=Cache,serive=" + serviceName + ",name=" + name + ",tier=" + tier;
		id = r.ensureGlobalName(id);
		r.unregister(id);		
	}
	
}
