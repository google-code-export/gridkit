package com.griddynamics.coherence.integration.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.griddynamics.coherence.integration.spring.config.CoherenceCacheScheme;
import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Service;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;

public class SpringDrivenCoherenceCacheFactory implements ConfigurableCacheFactory {

	private static final char SCOPE_SEPARATOR = '$';
	private static final String SPRING_BEAN_PREFIX = "spring:";

	/**
	 * Installs and/or retrieves instance of SpringDrivenCoherenceCacheFactory
	 */
	public static SpringDrivenCoherenceCacheFactory getInstance() {
		synchronized(CacheFactory.class) {
			ConfigurableCacheFactory factory = CacheFactory.getConfigurableCacheFactory();
			if (factory instanceof SpringDrivenCoherenceCacheFactory) {
				return (SpringDrivenCoherenceCacheFactory) factory;
			}
			else {
				// installs new instance
				if (CacheFactory.getCluster().isRunning()) {
					throw new IllegalStateException("Coherence cluster is already started");
				}
				if (!(factory instanceof DefaultConfigurableCacheFactory)) {
					throw new IllegalStateException("Coherence cache factory is already replaced by [" + factory + "]");
				}
				
				SpringDrivenCoherenceCacheFactory sdf = new SpringDrivenCoherenceCacheFactory();
				
				// TODO handle cache factory builder
				CacheFactory.setConfigurableCacheFactory(sdf);
				
				return sdf;
			}
		}
	}
	
	private Map<String, CoherenceCacheFactoryBean> contexts;	
	private FactoryDelegate factoryDelegate;
	
	private Cluster cluster;
	private CoherenceClusterConfigBean clusterConfigurationBean;
	
	private SpringDrivenCoherenceCacheFactory() {		
		cluster = CacheFactory.getCluster();
		contexts = new ConcurrentHashMap<String, CoherenceCacheFactoryBean>();
		factoryDelegate = new FactoryDelegate();
	}		

	public synchronized void registerContext(CoherenceCacheFactoryBean bean) {
		String scopeName = bean.getScopeName();		
		if (scopeName == null) {
			throw new IllegalArgumentException("illegal scope name <null>");
		}
		if (scopeName.indexOf(SCOPE_SEPARATOR) >= 0) {
			throw new IllegalArgumentException("illegal scope name '" + scopeName + "' - $ is disallowed in coherence names");
		}
		if(contexts.get(scopeName) != null) {
			throw new IllegalArgumentException("Scope '" + scopeName + "' is already defined by [" + contexts.get(scopeName) + "]");			
		}
		contexts.put(scopeName, bean);
	}
	
	public NamedCache getCache(String name, CoherenceCacheFactoryBean factory) {
		String cacheName = decorate(factory.getScopeName(), name);
		return ensureCache(cacheName, null);
	}

	public Service ensureService(String name, CoherenceCacheFactoryBean factory) {
		String cacheName = decorate(factory.getScopeName(), name);
		return ensureService(cacheName);
	}
	
	private String decorate(String scopeName, String name) {
		if (scopeName == null) {
			throw new IllegalArgumentException("illegal scope name <null>");
		}
		if (scopeName.indexOf(SCOPE_SEPARATOR) >= 0) {
			throw new IllegalArgumentException("illegal scope name '" + scopeName + "' - $ is disallowed in coherence names");
		}
		
		return scopeName.length() == 0 ? name : scopeName + SCOPE_SEPARATOR + name;
	}

	@Override
	public NamedCache ensureCache(String sCacheName, ClassLoader loader) {
		String scope = getScope(sCacheName);
		String name = getName(sCacheName);
		
		CoherenceCacheFactoryBean factory = getCacheFactory(sCacheName);
		
		return factoryDelegate.ensureCache(sCacheName, factory, name, loader);		
	}

	@Override
	public void destroyCache(NamedCache cache) {
		factoryDelegate.destroyCache(cache);
	}

	@Override
	public void releaseCache(NamedCache cache) {
		factoryDelegate.releaseCache(cache);		
	}

	@Override
	public Service ensureService(String sServiceName) {
		String scope = getScope(sServiceName);
		String name = getName(sServiceName);
		
		CoherenceCacheFactoryBean factory = contexts.get(scope);
		if (factory == null) {
			throw new IllegalArgumentException("No context found for scope '" + scope +"' while looking for definition of service '" + sServiceName + "'");
		}
		return factoryDelegate.ensureService(sServiceName, factory, name);		
	}
	
	private String getName(String sCacheName) {
		int p = sCacheName.indexOf(SCOPE_SEPARATOR);
		return p < 0 ? sCacheName : sCacheName.substring(p + 1);
	}
	
	private String getScope(String sCacheName) {
		int p = sCacheName.indexOf(SCOPE_SEPARATOR);
		return p < 0 ? "" : sCacheName.substring(0, p);
	}
	
	private CoherenceCacheFactoryBean getCacheFactory(String qname) {
		String scope = getScope(qname);
		CoherenceCacheFactoryBean factory = contexts.get(scope);
		if (factory == null) {
			throw new IllegalArgumentException("No context found for scope '" + scope +"' while looking for '" + qname + "'");
		}
		return factory;
	}

	@Override
	public XmlElement getConfig() {
		throw new UnsupportedOperationException("Configuration is Spring driven");
	}

	@Override
	public void setConfig(XmlElement xmlConfig) {
		throw new UnsupportedOperationException("Cannot set XML, configuration is Spring driven");
	}
	
	private class FactoryDelegate extends DefaultConfigurableCacheFactory {

		public FactoryDelegate() {
			// need to pass an xml element otherwise it will try to load one
			super(XmlHelper.loadXml("<cache-config></cache-config>"));
		}
		
		public NamedCache ensureCache(
				String sCacheName,
				CoherenceCacheFactoryBean factory, 
				String name,
				ClassLoader loader) {			
			// TODO review this later
			return super.ensureCache(sCacheName, loader);
		}

		public Service ensureService(
				String sServiceName,
				CoherenceCacheFactoryBean factory, 
				String name) {
			
			CoherenceServiceDefinition serviceDefinition = factory.getServiceDefinition(name);
			XmlElement xml = serviceDefinition.getXmlConfig(factory.getScopeName());
			return ensureService(xml);
		}

		@Override
		public CacheInfo findSchemeMapping(String sCacheName) {
			String name = getName(sCacheName);
			String scope = getScope(sCacheName);
			String schemeName = decorate(scope, name);
			// TODO additional cache attributes;
			Map map = new HashMap();
			CacheInfo ci = new CacheInfo(sCacheName, schemeName, map);
			return ci;
		}
		
		@Override
		public XmlElement resolveScheme(CacheInfo cacheInfo) {
			String schemaName = cacheInfo.getSchemeName();
			CoherenceCacheFactoryBean factory = getCacheFactory(schemaName);
			String name = getName(schemaName);
			String scope = getScope(schemaName);
			CoherenceCacheScheme cd = factory.getSchemeForCache(name);
			XmlElement xml = cd.getXmlConfig(scope);			
			return xml;
		}
		
		@Override
		protected XmlElement resolveScheme(XmlElement xmlScheme, CacheInfo info, boolean fChild, boolean fRequired) {
			// TODO potential incompatibility issues
			return xmlScheme;
		}

		@Override
		public NamedCache ensureCache(String sCacheName, ClassLoader loader) {
			return SpringDrivenCoherenceCacheFactory.this.ensureCache(sCacheName, loader);
		}

		@Override
		public Service ensureService(String sServiceName) {
			return SpringDrivenCoherenceCacheFactory.this.ensureService(sServiceName);
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
				
				String name = getName(sBeanName);
				CoherenceCacheFactoryBean factory = getCacheFactory(sBeanName);
				azzert(name != null && name.length() > 0, "Bean name required");

				// Handling bean context
				Object bean = factory.resolveBean(name);
				return bean;
			} else {
				return super.instantiateAny(info, xmlClass, context, loader);
			}
		}
	}
}
