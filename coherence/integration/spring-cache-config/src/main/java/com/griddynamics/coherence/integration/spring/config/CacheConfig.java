package com.griddynamics.coherence.integration.spring.config;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Dmitri Babaev
 */
@XmlRootElement(name="cache-config")
public class CacheConfig {
	
	@XmlElement(name="caching-scheme-mapping")
	public List<CacheMapping> cacheMappings;
	
	@XmlElement(name="caching-schemes")
	public List<CachingScheme> cachingSchemes;
}
