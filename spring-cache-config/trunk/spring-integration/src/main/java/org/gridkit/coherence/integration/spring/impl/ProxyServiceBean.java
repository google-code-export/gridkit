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

import org.gridkit.coherence.integration.spring.service.ProxyServiceConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Proxy service bean with custom wrapping logic
 * @author malexejev@gmail.com
 * 30.09.2010
 */
public class ProxyServiceBean extends ClusteredServiceBean implements ApplicationContextAware {
	
	private ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	/*
	 * set default implementation of cacheLookupStrategy (may be overriden in spring config)
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		ProxyServiceConfiguration proxyConfig = (ProxyServiceConfiguration) configuration;
		
		if (proxyConfig.getCacheLookupStrategy() == null) {
			SpringContextCacheLookupStrategy lookupStrategy = new SpringContextCacheLookupStrategy();
			lookupStrategy.setApplicationContext(applicationContext);
			proxyConfig.setCacheLookupStrategy(lookupStrategy);
		}
		
		super.afterPropertiesSet();
	}
	
}
