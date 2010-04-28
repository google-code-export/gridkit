package com.griddynamics.coherence.integration.spring;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.tangosol.net.AbstractBackingMapManager;
import com.tangosol.util.ObservableMap;

/**
 * @author Dmitri Babaev
 */
public class ContextBackingMapManager extends AbstractBackingMapManager {
	private ApplicationContext applicationContext;
	private Map<String, BackingMapDefinition> backingMapDefinitionsByCacheNames = new HashMap<String, BackingMapDefinition>();
	
	public ContextBackingMapManager(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public Map<?, ?> instantiateBackingMap(String cacheName) {
		BackingMapDefinition bmf = backingMapDefinitionsByCacheNames.get(cacheName);
		
		if (backingMapDefinitionsByCacheNames.get(cacheName) == null) {
			//Cache was created on another node, getting cache definition from the context
			bmf = applicationContext.getBean(cacheName, ServiceCacheDefinition.class).getBackingMapDefinition();
		}
		
		ObservableMap res = bmf.newBackingMap(getContext());
		return res;
	}
	
	void registerBackingMapDefinition(String cacheName, BackingMapDefinition definition) {
		if (backingMapDefinitionsByCacheNames.containsKey(cacheName)) {
			throw new IllegalArgumentException(String.format("backing map definition is already registred for cache '%s'", cacheName));
		}
		
		backingMapDefinitionsByCacheNames.put(cacheName, definition);
	}
}
