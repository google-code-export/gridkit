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

import org.gridkit.coherence.integration.spring.MapProvider;
import org.gridkit.coherence.integration.spring.NamedCacheDecorator;
import org.springframework.beans.factory.annotation.Required;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.NearCache;
import com.tangosol.net.management.Registry;

public class NearCacheDecorator implements NamedCacheDecorator {

	private Object frontSchemeBean;
	private InvalidationStrategy invalidationStrategy = InvalidationStrategy.auto;
	private NamedCacheDecorator nestedDecorator;
	
	@Required
	public void setFrontMap(Object frontSchemeBean) {
		this.frontSchemeBean = frontSchemeBean;
	}

	public void setInvalidationStrategy(InvalidationStrategy invalidationStrategy) {
		this.invalidationStrategy = invalidationStrategy;
	}

	public void setNestedDecorator(NamedCacheDecorator nestedDecorator) {
		this.nestedDecorator = nestedDecorator;
	}

	@Override
	public NamedCache wrapCache(NamedCache innerCache) {
		String name = innerCache.getCacheName();
		String serviceName = innerCache.getCacheService().getInfo().getServiceName();
		if (nestedDecorator != null) {
			innerCache = nestedDecorator.wrapCache(innerCache);
		}
		Map<?,?> map = MapProvider.Helper.getMapFromBean(frontSchemeBean, Map.class);
		NearCache cache = new NearCache(map, innerCache, invalidationStrategy.type());
		
		jmxRegister(map, name, serviceName, "front");
		
		return cache;
	}
	
	@Override
	public void releaseWrapper(NamedCache wrapped) {
		// TODO design cache life cycle
	}

	protected void jmxRegister(Map<?, ?> cache, String name, String service, String tier) {
		Registry r = CacheFactory.getCluster().getManagement();
		String id = "type=Cache,serive=" + service + ",name=" + name + ",tier=" + tier;
		id = r.ensureGlobalName(id);
		r.register(id, cache);		
	}

	protected void jmxUnregister(String name, String service, String tier) {
		Registry r = CacheFactory.getCluster().getManagement();
		String id = "type=Cache,serive=" + service + ",name=" + name + ",tier=" + tier;
		id = r.ensureGlobalName(id);
		r.unregister(id);		
	}
}
