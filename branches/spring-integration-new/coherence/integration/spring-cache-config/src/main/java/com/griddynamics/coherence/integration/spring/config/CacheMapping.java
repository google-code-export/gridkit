package com.griddynamics.coherence.integration.spring.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Dmitri Babaev
 */
@XmlType(name="cache-mapping")
@XmlAccessorType(XmlAccessType.NONE)
public class CacheMapping {
	private String cacheName;
	private CachingScheme cacheScheme;
	
	public CacheMapping() {
	}
	
	public CacheMapping(String cacheName, CachingScheme cacheScheme) {
		this.cacheName = cacheName;
		this.cacheScheme = cacheScheme;
	}
	
	@XmlElement(name="cache-name")
	public String getCacheName() {
		return cacheName;
	}
	
	public CachingScheme getCachingScheme() {
		return cacheScheme;
	}
	
	@XmlElement(name="scheme-name")
	public String getCachingSchemeName() {
		return cacheScheme.getName();
	}
}
