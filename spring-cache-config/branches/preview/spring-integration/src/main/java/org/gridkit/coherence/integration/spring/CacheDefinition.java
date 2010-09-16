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
package org.gridkit.coherence.integration.spring;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import com.tangosol.net.NamedCache;

/**
 * @author malexejev@gmail.com
 * 15.09.2010
 */
public class CacheDefinition implements InitializingBean, BeanNameAware, MapProvider {

	private String cacheName;
	
	private NamedCacheDecorator frontTier;
	private ClusteredCacheService clusteredService;
	
	protected CountDownLatch initGate = new CountDownLatch(1);
	
	@Override
	public void setBeanName(String name) {
		this.cacheName = name;
	}
	
	public void setFrontTier(NamedCacheDecorator frontTier) {
		this.frontTier = frontTier;
	}
	
	@Required
	public void setService(ClusteredCacheService service) {
		this.clusteredService = service;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		initGate.countDown();
	}
	
	@Override
	public Map<?, ?> getMap() {
		return getCache();
	}

	public NamedCache getCache() {
		// should wait bean to be initialized
		try {
			initGate.await();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
//		if (!initialized) {
//			System.out.println("By thread " + threadName);
//			callSite.printStackTrace();
//			throw new IllegalStateException("Cache definition is not initialized");
//		}
		try {
			NamedCache cache = clusteredService.ensureCache(cacheName);
			if (frontTier != null) {
				cache = frontTier.wrapCache(cache);
			}
			return cache;
		}
		catch(RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
	}

}
