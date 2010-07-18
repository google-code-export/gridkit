package org.gridkit.coherence.integration.spring.cache;

import java.util.Map;

import org.gridkit.coherence.integration.spring.BackningMapProvider;
import org.springframework.beans.factory.InitializingBean;

import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.cache.CacheLoader;
import com.tangosol.net.cache.ReadWriteBackingMap;
import com.tangosol.util.ObservableMap;

/**
 * @author Dmitri Babaev
 */
public class ReadWriteBackingMapDefinition implements BackningMapProvider, InitializingBean {
	
	private Object internalMap;
	private Object missesMap;
	private CacheLoader loader;
	private boolean readOnly = false;
	private int writeBehindSeconds = 0;
	private double refreshAheadFactor = 0.0D;
	
	private ObservableMap instance;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO validation
	}

	@Override
	public Map<?, ?> getBackinMap(BackingMapManagerContext context) {
		ensureBackingMap(context);
		return instance;
	}

	private synchronized void ensureBackingMap(BackingMapManagerContext cacheCtx) {
		ObservableMap internal = BackningMapProvider.Helper.getMapFromBean(internalMap, cacheCtx, ObservableMap.class);
		Map<?, ?> misses = missesMap == null ? null : BackningMapProvider.Helper.getMapFromBean(missesMap, cacheCtx, Map.class);
		
		instance = new ReadWriteBackingMap(cacheCtx, internal, misses, loader, readOnly, writeBehindSeconds, refreshAheadFactor);
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
