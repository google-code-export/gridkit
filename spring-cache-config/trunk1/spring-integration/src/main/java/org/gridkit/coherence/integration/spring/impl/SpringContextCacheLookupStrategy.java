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

import org.gridkit.coherence.integration.spring.CacheLookupStrategy;
import org.gridkit.coherence.integration.spring.ClusteredCacheDefinition;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.tangosol.net.NamedCache;

/**
 * Manages caches stored as beans in a Spring application context
 * @author malexejev@gmail.com
 * 27.09.2010
 */
public class SpringContextCacheLookupStrategy implements CacheLookupStrategy, ApplicationContextAware {

	private ApplicationContext context;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

	@Override
	public void destroyCache(NamedCache cache) {
		throw new UnsupportedOperationException("Unexpected attempt to destroy cache " + cache.getCacheName());
	}
	
	@Override
	public NamedCache ensureCache(String sCacheName, ClassLoader loader) {
		ClusteredCacheDefinition definition = (ClusteredCacheDefinition) context.getBean(sCacheName);
		return definition.getCache();
	}

}
