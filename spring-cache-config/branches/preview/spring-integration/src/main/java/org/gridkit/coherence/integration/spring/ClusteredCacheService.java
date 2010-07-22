package org.gridkit.coherence.integration.spring;

import com.tangosol.net.CacheService;
import com.tangosol.net.NamedCache;

public interface ClusteredCacheService {

	public NamedCache ensureCache(String name);
	
	public void destroyCahce(NamedCache cahce);
	
	public CacheService getCoherenceService();	
	
}
