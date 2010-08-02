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

import org.gridkit.coherence.integration.spring.BackingMapLookupStrategy;
import org.gridkit.coherence.integration.spring.BackningMapProvider;
import org.gridkit.coherence.integration.spring.ClusteredCacheDefinition;
import org.gridkit.coherence.integration.spring.MapProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.tangosol.net.BackingMapManagerContext;

public class ByNameBackingMapLookupStrategy implements BackingMapLookupStrategy, ApplicationContextAware {

	private String template = "{cache-name}";
	private ApplicationContext appContext;
	
	public void setNameTemplate(String template) {
		this.template = template;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.appContext = applicationContext;
	}

	@Override
	public Map<?, ?> instantiateBackingMap(String cacheName, BackingMapManagerContext context) {
		String name = template.replaceAll("[{]cache-name[}]", cacheName);
		Object bean = appContext.getBean(name);
		Map<?, ?> map;
		if (bean instanceof ClusteredCacheDefinition) {
			map = ((ClusteredCacheDefinition)bean).getBackendInstance(appContext, context);
		}
		else if (bean instanceof BackningMapProvider) {
			map = ((BackningMapProvider)bean).getBackinMap(context);
		}
		else if (bean instanceof MapProvider) {
			map = ((MapProvider)bean).getMap();
		}
		else if (bean instanceof Map<?, ?>) {
			map = (Map<?, ?>) bean;
		}
		else {
			throw new IllegalArgumentException("Cannot convert to backing map object of type " + bean.getClass().getName());
		}
		return map;
	}
	
	@Override
	public void disposeBackingMap(String cacheName, Map<?, ?> backingMap) {
		// do nothing
	}
}
