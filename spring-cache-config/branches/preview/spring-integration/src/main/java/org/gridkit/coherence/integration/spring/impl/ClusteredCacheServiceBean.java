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
import org.gridkit.coherence.integration.spring.ClusteredCacheService;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import com.tangosol.net.AbstractBackingMapManager;
import com.tangosol.net.BackingMapManager;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.CacheService;
import com.tangosol.net.Cluster;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Service;
import com.tangosol.net.management.Registry;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class ClusteredCacheServiceBean extends ClusteredServiceBean implements ClusteredCacheService, InitializingBean, BeanNameAware, DisposableBean {

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

	protected CacheService getCacheService() {
		return ((CacheService)service);
	}
	
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
	public void destroyCahce(NamedCache cache) {
		getCacheService().destroyCache(cache);
		
	}

	private synchronized void ensureStarted() {
		if (this.service == null) {
			serviceName = serviceName == null ? beanName : serviceName;
			final Cluster cluster = CacheFactory.ensureCluster();
			final Service service = cluster.ensureService(serviceName, configuration.getServiceType().toString());
			synchronized(cluster) {
				if (!service.isRunning()) {
					service.configure(configuration.getXmlConfiguration());
					configuration.postConfigure(service);						
					((CacheService)service).setBackingMapManager(bmm);					
				}
			}
			if (!service.isRunning()) {
				threadHelper.modalExecute(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						synchronized(cluster) {
							if (!service.isRunning()) {
								service.start();
							}
							return null;
						}   
					}
				});
			}
			if (((CacheService)service).getBackingMapManager() != bmm) {
				throw new IllegalArgumentException("Service name conflict. Service [" + serviceName + "] is owned by other service bean");
			}
			this.service = (CacheService) service;
		}
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

	protected void jmxRegister(Map<?, ?> cache, String name, String tier) {
		Registry r = service.getCluster().getManagement();
		String id = "type=Cache,serive=" + service.getInfo().getServiceName() + ",name=" + name + ",tier=" + tier;
		id = r.ensureGlobalName(id);
		r.register(id, cache);		
	}

	protected void jmxUnregister(String name, String tier) {
		Registry r = service.getCluster().getManagement();
		String id = "type=Cache,serive=" + serviceName + ",name=" + name + ",tier=" + tier;
		id = r.ensureGlobalName(id);
		r.unregister(id);		
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
			jmxRegister(backingMap, cacheName, "back");
			return backingMap;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void releaseBackingMap(String sName, Map map) {
			super.releaseBackingMap(sName, map);
			jmxUnregister(sName, "back");
		}
	}
}
