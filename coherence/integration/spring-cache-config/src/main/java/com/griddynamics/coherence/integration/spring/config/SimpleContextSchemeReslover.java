package com.griddynamics.coherence.integration.spring.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Dmitri Babaev
 */
public class SimpleContextSchemeReslover implements CacheSchemeResolver, ApplicationContextAware {
	private ApplicationContext applicationContext;
	private Map<String, String> cacheSchemes = new HashMap<String, String>();

	public CacheScheme getSchemeByCacheName(String cacheName) {
		String schemeId = cacheSchemes.get(cacheName);
		if (schemeId == null)
			throw new IllegalArgumentException(String.format("can't find scheme for cache '%s'", cacheName));
		return applicationContext.getBean(schemeId, CacheScheme.class);
	}
	
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	@Required
	public void setCacheSchemes(Map<String, String> cacheSchemes) {
		this.cacheSchemes = cacheSchemes;
	}
}
