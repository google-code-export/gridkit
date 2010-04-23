package com.griddynamics.coherence.integration.spring.config;

import java.util.Map;

import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.cache.CacheLoader;
import com.tangosol.net.cache.ReadWriteBackingMap;
import com.tangosol.util.ObservableMap;

/**
 * @author Dmitri Babaev
 */
public class ReadWriteBackingMapDefinition implements BackingMapFactory {
	private ObservableMap internalMap;
	private Map<?, ?> missesMap;
	private CacheLoader loader;
	private boolean readOnly = false;
	private int writeBehindSeconds = 0;
	private double refreshAheadFactor = 0.0D;

	public ObservableMap newBackingMap(BackingMapManagerContext context) {
		return new ReadWriteBackingMap(context, internalMap, missesMap, loader, readOnly, writeBehindSeconds, refreshAheadFactor);
	}
	
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	public void setLoader(CacheLoader loader) {
		this.loader = loader;
	}
	
	public void setRefreshAheadFactor(double refreshAheadFactor) {
		this.refreshAheadFactor = refreshAheadFactor;
	}
	
	public void setInternalMap(ObservableMap internalMap) {
		this.internalMap = internalMap;
	}
	
	public void setMissesMap(Map<?, ?> missesMap) {
		this.missesMap = missesMap;
	}
	
	public void setWriteBehindSeconds(int writeBehindSeconds) {
		this.writeBehindSeconds = writeBehindSeconds;
	}
}
