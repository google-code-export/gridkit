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

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.griddynamics.coherence.integration.spring.config.DistributeCacheServiceDefinition;
import com.griddynamics.coherence.integration.spring.config.DistributedSchemeBean;
import com.tangosol.net.NamedCache;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class CoherenceNamespaceHandler extends NamespaceHandlerSupport {

	public CoherenceNamespaceHandler() {
    }

	@Override
	public void init() {
		registerBeanDefinitionParser("cache-factory", new CacheFactoryDefinitionParser());
        registerBeanDefinitionParser("cache", new CacheDefinitionParser());
//        registerBeanDefinitionParser("local-scheme", new LocalSchemeDefinitionParser());
        registerBeanDefinitionParser("distributed-scheme", new DistributedSchemeDefinitionParser());
        registerBeanDefinitionParser("distributed-service", new DistributedServiceDefinitionParser());
	}

    class CacheFactoryDefinitionParser extends AbstractSimpleBeanDefinitionParser {

		@Override
		protected Class<?> getBeanClass(Element element) {
			return CoherenceCacheFactoryBean.class;
		}

		@Override
		protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
			builder.addPropertyValue("id", "coherenceCacheFactory");
			String scopeName = getString(element, "scope");
			builder.addPropertyValue("scopeName", scopeName == null ? "" : scopeName);
//			if (exists(element, "lazy")) {
//				builder.setLazyInit(true);
//			}
		}
	}

    class CacheDefinitionParser extends AbstractSimpleBeanDefinitionParser {
    	
    	@Override
    	protected Class<?> getBeanClass(Element element) {
    		return NamedCache.class;
    	}
    	
    	@Override
    	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
    		builder.getRawBeanDefinition().setFactoryBeanName("coherenceCacheFactory");
    		builder.getRawBeanDefinition().setFactoryMethodName("getCache");

//    		if (exists(element, "lazy") || getBoolean(element,"@lazy")) {
//    			builder.setLazyInit(true);
//    		}
    		String scheme = getString(element, "@scheme");
    		builder.addConstructorArgReference(scheme);
    	}
    }

    class DistributedSchemeDefinitionParser extends AbstractSimpleBeanDefinitionParser {
    	
    	@Override
    	protected Class<?> getBeanClass(Element element) {
    		return DistributedSchemeBean.class;
    	}
    	
    	@Override
    	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {    	    		
//    		if (exists(element, "lazy") || getBoolean(element,"@lazy")) {
//    			builder.setLazyInit(true);
//    		}
//    		String scheme = getString("@sheme");
//    		builder.addConstructorArgReference(scheme);
    	}
    }

    class DistributedServiceDefinitionParser extends AbstractSimpleBeanDefinitionParser {
    	
    	@Override
    	protected Class<?> getBeanClass(Element element) {
    		return DistributeCacheServiceDefinition.class;
    	}
    	
    	@Override
    	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {    	    		
    	}
    }

    static String getString(Element element, String path) {
    	return null;
    }

//    static boolean getBoolean(Element element, String path) {
//    	
//    }
//
//    static boolean exists(Element element, String path) {
//    	
//    }
}
