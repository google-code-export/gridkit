package org.gridkit.coherence.txlite;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.tangosol.coherence.component.util.collections.wrapperMap.WrapperNamedCache;
import com.tangosol.net.CacheService;
import com.tangosol.net.MemberListener;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.CacheStatistics;
import com.tangosol.net.cache.CachingMap;
import com.tangosol.net.cache.NearCache;
import com.tangosol.run.xml.XmlConfigurable;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.util.ConcurrentMap;
import com.tangosol.util.Filter;
import com.tangosol.util.MapListener;
import com.tangosol.util.ValueExtractor;

public class TxLiteCache extends NearCache implements XmlConfigurable, TxWrappedCache {

	private final static Map FAKE_MAP = new HashMap();
	private final static WrapperNamedCache FAKE_CACHE = new WrapperNamedCache() {

		@Override
		public String getCacheName() {
			return "fake";
		}

		@Override
		public CacheService getCacheService() {
			return null;
		}
	};
	
	protected NamedCache cache;
	protected TxCacheWrapper proxy;
	
	protected XmlElement config;
	
	public TxLiteCache(Map mapFront, NamedCache mapBack) {
		super(FAKE_MAP, FAKE_CACHE);
		cache = createNearCache(mapFront, mapBack, CachingMap.LISTEN_AUTO);
		reinit();
	}	

	public TxLiteCache(Map mapFront, NamedCache mapBack, int nStrategy) {
		super(FAKE_MAP, FAKE_CACHE);
		cache = createNearCache(mapFront, mapBack, nStrategy);
		reinit();
	}
	
	@Override
	public XmlElement getConfig() {
		return config;
	}

	@Override
	public void setConfig(XmlElement config) {
		this.config = config;
		reinit();		
	}

	protected void reinit() {
		proxy = new TxCacheWrapper(cache, new DirtyReadCacheAccessAdapter());
	}

	protected NamedCache createNearCache(Map mapFront, NamedCache mapBack, int nStrategy) {
		return new NearCache(mapFront, mapBack, nStrategy);
	}

	// delegates of TxCacheWrapper
	
	@Override
	public NamedCache getVersionedCache() {
		return cache;
	}

	public void addIndex(ValueExtractor extractor, boolean fOrdered,
			Comparator comparator) {
		proxy.addIndex(extractor, fOrdered, comparator);
	}

	public void addMapListener(MapListener listener, Filter filter, boolean lite) {
		proxy.addMapListener(listener, filter, lite);
	}

	public void addMapListener(MapListener listener, Object key, boolean lite) {
		proxy.addMapListener(listener, key, lite);
	}

	public void addMapListener(MapListener listener) {
		proxy.addMapListener(listener);
	}

	public Object aggregate(Collection collKeys, EntryAggregator agent) {
		return proxy.aggregate(collKeys, agent);
	}

	public Object aggregate(Filter filter, EntryAggregator agent) {
		return proxy.aggregate(filter, agent);
	}

	public void clear() {
		proxy.clear();
	}

	public boolean containsKey(Object key) {
		return proxy.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return proxy.containsValue(value);
	}

	public void destroy() {
		proxy.destroy();
	}

	public Set entrySet() {
		return proxy.entrySet();
	}

	public Set entrySet(Filter filter, Comparator comparator) {
		return proxy.entrySet(filter, comparator);
	}

	public Set entrySet(Filter filter) {
		return proxy.entrySet(filter);
	}

	public boolean equals(Object obj) {
		return proxy.equals(obj);
	}

	public Object get(Object oKey) {
		return proxy.get(oKey);
	}

	public Map getAll(Collection keys) {
		return proxy.getAll(keys);
	}

	public String getCacheName() {
		return proxy.getCacheName();
	}

	public CacheService getCacheService() {
		return proxy.getCacheService();
	}

	public int hashCode() {
		return proxy.hashCode();
	}

	public Object invoke(Object key, EntryProcessor agent) {
		return proxy.invoke(key, agent);
	}

	public Map invokeAll(Collection keys, EntryProcessor agent) {
		return proxy.invokeAll(keys, agent);
	}

	public Map invokeAll(Filter filter, EntryProcessor agent) {
		return proxy.invokeAll(filter, agent);
	}

	public boolean isActive() {
		return proxy.isActive();
	}

	public boolean isEmpty() {
		return proxy.isEmpty();
	}

	public Set keySet() {
		return proxy.keySet();
	}

	public Set keySet(Filter paramFilter) {
		return proxy.keySet(paramFilter);
	}

	public boolean lock(Object key, long timeout) {
		return proxy.lock(key, timeout);
	}

	public boolean lock(Object key) {
		return proxy.lock(key);
	}

	public Object put(Object key, Object value, long expiry) {
		return proxy.put(key, value, expiry);
	}

	public Object put(Object key, Object value) {
		return proxy.put(key, value);
	}

	public void putAll(Map m) {
		proxy.putAll(m);
	}

	public void release() {
		proxy.release();
	}

	public Object remove(Object key) {
		return proxy.remove(key);
	}

	public void removeIndex(ValueExtractor extractor) {
		proxy.removeIndex(extractor);
	}

	public void removeMapListener(MapListener listener, Filter filter) {
		proxy.removeMapListener(listener, filter);
	}

	public void removeMapListener(MapListener listener, Object key) {
		proxy.removeMapListener(listener, key);
	}

	public void removeMapListener(MapListener listener) {
		proxy.removeMapListener(listener);
	}

	public int size() {
		return proxy.size();
	}

	public String toString() {
		return proxy.toString();
	}

	public boolean unlock(Object key) {
		return proxy.unlock(key);
	}

	public Collection values() {
		return proxy.values();
	}
	
	// delegates of NearCache
	
	public Map getBackMap() {
		return ((CachingMap)cache).getBackMap();
	}

	public CacheStatistics getCacheStatistics() {
		return ((CachingMap)cache).getCacheStatistics();
	}

	public ConcurrentMap getControlMap() {
		return ((CachingMap)cache).getControlMap();
	}

	public Map getFrontMap() {
		return ((CachingMap)cache).getFrontMap();
	}

	public long getInvalidationHits() {
		return ((CachingMap)cache).getInvalidationHits();
	}

	public long getInvalidationMisses() {
		return ((CachingMap)cache).getInvalidationMisses();
	}

	public int getInvalidationStrategy() {
		return ((CachingMap)cache).getInvalidationStrategy();
	}

	public long getTotalRegisterListener() {
		return ((CachingMap)cache).getTotalRegisterListener();
	}

	public Object put(Object oKey, Object oValue, boolean fReturn, long cMillis) {
		return ((CachingMap)cache).put(oKey, oValue, fReturn, cMillis);
	}
	
	@Override
	public NamedCache getBackCache() {
		return ((NearCache)cache).getBackCache();
	}

	@Override
	public ClassLoader getContextClassLoader() {
		return ((NearCache)cache).getContextClassLoader();
	}

	@Override
	public void setContextClassLoader(ClassLoader loader) {
		((NearCache)cache).setContextClassLoader(loader);
	}

	@Override
	protected MemberListener registerBackServiceListener() {
		// do nothing
		return null;
	}

	@Override
	protected void unregisterBackServiceListener() {
		// do nothing;
	}	
}
