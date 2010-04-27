package com.griddynamics.coherence.integration.spring.config;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Required;

import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.CacheService;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Base;
import com.tangosol.util.MapListener;
import com.tangosol.util.ObservableMap;

/**
 * @author Dmitri Babaev
 */
public class ServiceCacheDefinition implements BeanNameAware, CacheDefinition {
	private String cacheName;
	private CacheService service;
	private MapListener mapListener;
	private BackingMapFactory backingMapFactory;
	private ObservableMap backingMap;
	
	public NamedCache newCache() {
		NamedCache cache = service.ensureCache(cacheName, getCacheClassLoader());
		if (mapListener != null)
			cache.addMapListener(mapListener);
		return cache;
	}

	protected ClassLoader getCacheClassLoader() {
		return Base.getContextClassLoader();
	}

	public void setBeanName(String name) {
		this.cacheName = name;
	}
	
	public void setMapListener(MapListener mapListener) {
		this.mapListener = mapListener;
	}
	
	@Required
	public void setCacheService(CacheService service) {
		this.service = service;
	}
	
	public void setBackingMapFactory(BackingMapFactory backingMapFactory) {
		this.backingMapFactory = backingMapFactory;
	}
	
	public void setBackingMap(ObservableMap backingMap) {
		this.backingMap = backingMap;
	}
	
	public BackingMapFactory getBackingMapFactory() {
		if (backingMapFactory != null)
			return backingMapFactory;
		
		if (backingMap != null) {
			return new BackingMapFactory() {
				public ObservableMap newBackingMap(BackingMapManagerContext context) {
					return backingMap;
				}
			};
		}
		
		throw new IllegalStateException("backing map or backing map factory is not set");
	}
}
