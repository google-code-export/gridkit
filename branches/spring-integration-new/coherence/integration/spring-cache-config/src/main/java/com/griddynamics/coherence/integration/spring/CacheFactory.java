package com.griddynamics.coherence.integration.spring;

import com.tangosol.net.NamedCache;

/**
 * @author Dmitri Babaev
 */
public interface CacheFactory {

	NamedCache newCache(String cacheName);
}
