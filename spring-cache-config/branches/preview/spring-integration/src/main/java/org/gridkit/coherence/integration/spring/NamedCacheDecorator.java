package org.gridkit.coherence.integration.spring;

import com.tangosol.net.NamedCache;

public interface NamedCacheDecorator {

	public NamedCache wrapCache(NamedCache innerCache);
	
}
