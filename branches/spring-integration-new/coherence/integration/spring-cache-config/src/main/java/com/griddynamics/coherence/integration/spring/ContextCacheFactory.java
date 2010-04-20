package com.griddynamics.coherence.integration.spring;

import java.util.Collections;
import java.util.HashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Service;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;

public class ContextCacheFactory implements ConfigurableCacheFactory, ApplicationContextAware {
	private static final char SCOPE_SEPARATOR = '$';
	private static final String SPRING_BEAN_PREFIX = "spring:";
	
	private FactoryDelegate factory = new FactoryDelegate();
	private String scopeName;
	private ApplicationContext applicationContext;
	
	public void setScopeName(String scopeName) {
		if (scopeName == null) {
			throw new IllegalArgumentException("illegal scope name: <null>");
		}
		if (scopeName.indexOf(SCOPE_SEPARATOR) >= 0) {
			throw new IllegalArgumentException(
				String.format("illegal scope name '%s' - %s is disallowed in coherence names", scopeName, SCOPE_SEPARATOR));
		}
		
		this.scopeName = scopeName;
	}
	
	private String decorate(String name) {
		return scopeName.length() == 0 ? name : scopeName + SCOPE_SEPARATOR + name;
	}
	
	private String extractName(String sCacheName) {
		int p = sCacheName.indexOf(SCOPE_SEPARATOR);
		return p < 0 ? sCacheName : sCacheName.substring(p + 1);
	}

	public NamedCache ensureCache(String cacheName, ClassLoader loader) {
		return factory.ensureCache(decorate(cacheName), loader);
	}
	
	public Service ensureService(String serviceName) {
		return factory.ensureService(decorate(serviceName));
	}
	
	public void destroyCache(NamedCache cache) {
		factory.destroyCache(cache);
	}
	
	public void releaseCache(NamedCache cache) {
		factory.releaseCache(cache);
	}
	
	public XmlElement getConfig() {
		throw new UnsupportedOperationException("Configuration is Spring driven");
	}

	public void setConfig(XmlElement xmlConfig) {
		throw new UnsupportedOperationException("Cannot set XML, configuration is Spring driven");
	}
	
	public void setApplicationContext(ApplicationContext applicationContext)
		throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	private BeanDefinition findSchemeDefinitionForCache(String cacheName) {
		String name = extractName(cacheName);
		//DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getBeanFactory();
		return null;
	}
	
	class FactoryDelegate extends DefaultConfigurableCacheFactory {
		public FactoryDelegate() {
			// need to pass an XML element otherwise it will try to load one
			super(XmlHelper.loadXml("<cache-config></cache-config>"));
		}
		
		@Override
		public CacheInfo findSchemeMapping(String sCacheName) {
			CacheInfo ci = new CacheInfo(sCacheName, sCacheName, Collections.emptyMap());
			return ci;
		}
		
		@Override
		public XmlElement resolveScheme(CacheInfo cacheInfo) {
			//DefaultConfigurableCacheFactory.ensureCache --> resolveScheme
			//DefaultConfigurableCacheFactory.Manager.instantiateBackingMap --> resolveScheme
			
			BeanDefinition schemeDefinition = findSchemeDefinitionForCache(cacheInfo.getCacheName());
			
			XmlElement xml = XmlHelper.loadXml("<cache-config></cache-config>");			
			return xml;
		}
		
		@Override
		public Object instantiateAny(
				CacheInfo info, 
				XmlElement xmlClass,
				BackingMapManagerContext context, 
				ClassLoader loader) {
			
			if (translateSchemeType(xmlClass.getName()) != SCHEME_CLASS) {
				throw new IllegalArgumentException("Invalid class definition: "	+ xmlClass);
			}
			String sClass = xmlClass.getSafeElement("class-name").getString();

			if (sClass.startsWith(SPRING_BEAN_PREFIX)) {
				String sBeanName = sClass.substring(SPRING_BEAN_PREFIX.length());
				azzert(sBeanName != null && sBeanName.length() > 0, "Bean name required");

				// Handling bean applicationContext
				Object bean = applicationContext.getBean(sBeanName);
				return bean;
			} else {
				return super.instantiateAny(info, xmlClass, context, loader);
			}
		}
	}
}
