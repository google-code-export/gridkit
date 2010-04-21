package com.griddynamics.coherence.integration.spring;

import com.griddynamics.coherence.integration.spring.config.CacheScheme;
import com.griddynamics.coherence.integration.spring.config.DistributedScheme;
import com.tangosol.run.xml.SimpleElement;
import com.tangosol.run.xml.XmlElement;

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
		schemeEl.addElement("backing-map-scheme").setString(cacheScheme.getServiceName());
		return null;
	}
	
	static public String decorateBeanId(String beanId) {
		return SPRING_BEAN_PREFIX+beanId;
	}
}
