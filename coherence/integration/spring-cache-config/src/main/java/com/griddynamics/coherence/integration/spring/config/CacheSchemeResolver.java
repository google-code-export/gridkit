package com.griddynamics.coherence.integration.spring.config;

/**
 * @author Dmitri Babaev
 */
public interface CacheSchemeResolver {
	CacheScheme getSchemeByCacheName(String cacheName);
}
