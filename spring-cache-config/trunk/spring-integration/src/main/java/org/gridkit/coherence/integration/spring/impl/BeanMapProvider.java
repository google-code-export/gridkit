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

import org.gridkit.coherence.integration.spring.MapProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class BeanMapProvider implements MapProvider, ApplicationContextAware {

	private ApplicationContext appContext;
	private String beanName;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.appContext = applicationContext;
	}
	
	@Required
	public void setBean(String name) {
		this.beanName = name;
	}
	
	@Override	
	public Map<?, ?> getMap() {
		return MapProvider.Helper.getMapFromBean(appContext.getBean(beanName), Map.class);
	}
}
