package com.griddynamics.coherence.integration.spring;

import com.tangosol.net.NamedCache;

/**
 * @author Dmitri Babaev
 */
public interface CacheDefinition {

	public NamedCache newCache();
}