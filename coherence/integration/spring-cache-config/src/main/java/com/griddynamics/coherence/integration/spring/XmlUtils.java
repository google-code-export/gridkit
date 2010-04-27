package com.griddynamics.coherence.integration.spring;

import com.griddynamics.coherence.integration.spring.config.CacheScheme;
import com.griddynamics.coherence.integration.spring.config.DistributedScheme;
import com.tangosol.run.xml.SimpleElement;
import com.tangosol.run.xml.XmlElement;

/**
 * @author Dmitri Babaev
 */
public class XmlUtils {
	public static final String SPRING_BEAN_PREFIX = "spring:";
	
	static public XmlElement buildCacheSchemeConfig(CacheScheme cacheScheme) {
		if (cacheScheme instanceof DistributedScheme)
			return buildDistributedCacheConfig((DistributedScheme)cacheScheme);
		
		throw new IllegalArgumentException(String.format("can't build configuration for cache scheme '%s'", cacheScheme.getServiceName()));
	}
	
	static public XmlElement buildDistributedCacheConfig(DistributedScheme cacheScheme) {
		XmlElement schemeEl = new SimpleElement("distributed-scheme");
		
		cacheScheme.getServiceName();
		schemeEl.addElement("service-name").setString(cacheScheme.getServiceName());
		addBeanReference(schemeEl.addElement("backing-map-scheme"), cacheScheme.getBackingMapId());
		
		if (cacheScheme.getListenerId() != null)
			addBeanReference(schemeEl.addElement("listener"), cacheScheme.getListenerId());
		
		if (cacheScheme.getSerializerId() != null)
			addBeanReference(schemeEl.addElement("serializer"), cacheScheme.getSerializerId());
		
		if (cacheScheme.getThreadCount() != null)
			schemeEl.addElement("thread-count").setInt(cacheScheme.getThreadCount());
			
		return schemeEl;
	}
	
	static public String decorateBeanId(String beanId) {
		return SPRING_BEAN_PREFIX+beanId;
	}
	
	static public void addBeanReference(XmlElement element, String beanId) {
		element.addElement("class-scheme").addElement("class-name").setString(decorateBeanId(beanId));
	}
}
