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
package com.griddynamics.coherence.integration.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.griddynamics.coherence.integration.spring.config.CoherenceCacheScheme;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Service;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class CoherenceCacheFactoryBean implements ApplicationContextAware, InitializingBean {

	private ApplicationContext springContext;
	private SpringDrivenCoherenceCacheFactory cacheFactory;
	private String coherenceNameSpace = "";
	
	public String getScopeName() {
		return coherenceNameSpace;
	}

	public void setScopeName(String name) {
		this.coherenceNameSpace = name;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		this.springContext = ctx;		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		cacheFactory = SpringDrivenCoherenceCacheFactory.getInstance();
		cacheFactory.registerContext(this);
	}

	public NamedCache getCache(String name, CoherenceCacheScheme scheme) {
		return cacheFactory.getCache(name, this);
	}

	public Service ensureService(String service) {
		return cacheFactory.ensureService(service, this); 
	}
	
	public CoherenceServiceDefinition getServiceDefinition(String name) {
		return (CoherenceServiceDefinition)springContext.getBean(name);
	}

	public CoherenceCacheScheme getSchemeForCache(String name) {
		return (CoherenceCacheScheme)springContext.getBean(name);
	}

	public Object resolveBean(String name) {
		return springContext.getBean(name);		
	}		
}
