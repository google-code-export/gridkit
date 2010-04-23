package com.griddynamics.coherence.integration.spring;

import java.util.Collections;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.griddynamics.coherence.integration.spring.config.BackingMapFactory;
import com.griddynamics.coherence.integration.spring.config.CacheDefinition;
import com.griddynamics.coherence.integration.spring.config.CacheScheme;
import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;

/**
 * @author Dmitri Babaev
 */
public class ContextCacheFactory implements CacheFactory, ApplicationContextAware {
	private static final char SCOPE_SEPARATOR = '$';
	
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

	public NamedCache newCache(String cacheName) {
		return factory.ensureCache(decorate(cacheName), null);
	}
	
	public void setApplicationContext(ApplicationContext applicationContext)
		throws BeansException {
		this.applicationContext = applicationContext;
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
			
			CacheScheme cacheScheme = applicationContext.getBean(extractName(cacheInfo.getCacheName()), CacheDefinition.class).getCacheScheme();
			XmlElement schemeDefinition = XmlUtils.buildCacheSchemeConfig(cacheScheme);
			return schemeDefinition;
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

			if (sClass.startsWith(XmlUtils.SPRING_BEAN_PREFIX)) {
				String beanName = sClass.substring(XmlUtils.SPRING_BEAN_PREFIX.length());
				azzert(beanName != null && beanName.length() > 0, "Bean name required");

				// Handling bean applicationContext
				Object bean = applicationContext.getBean(beanName);
				
				if (bean instanceof BackingMapFactory) {
					bean = ((BackingMapFactory) bean).newBackingMap(context);
				}
				
				return bean;
			} else {
				return super.instantiateAny(info, xmlClass, context, loader);
			}
		}
	}
}
