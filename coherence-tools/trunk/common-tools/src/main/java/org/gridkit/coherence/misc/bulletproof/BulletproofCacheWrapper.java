package org.gridkit.coherence.misc.bulletproof;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import com.tangosol.net.CacheService;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.MapListener;
import com.tangosol.util.ValueExtractor;

public class BulletproofCacheWrapper implements NamedCache {
	
	private NamedCache cache;
	private int recoveryAttempts = 1;
	
	public BulletproofCacheWrapper(NamedCache cache) {
		this.cache = cache;		
	}

	public int getRecoveryAttempts() {
		return recoveryAttempts;
	}

	public void setRecoveryAttempts(int recoveryAttempts) {
		this.recoveryAttempts = recoveryAttempts;
	}

	public void addMapListener(MapListener listener) {
		cache.addMapListener(listener);
	}

	public void removeMapListener(MapListener listener) {
		cache.removeMapListener(listener);
	}

	@SuppressWarnings("rawtypes")
	public Set keySet(Filter filter) {
		return cache.keySet(filter);
	}

	public Object invoke(Object key, EntryProcessor agent) {
		return cache.invoke(key, agent);
	}

	@SuppressWarnings("rawtypes")
	public Set entrySet(Filter filter) {
		return cache.entrySet(filter);
	}

	public void addMapListener(MapListener listener, Object key, boolean lite) {
		cache.addMapListener(listener, key, lite);
	}

	@SuppressWarnings("rawtypes")
	public Map invokeAll(Collection keys, EntryProcessor agent) {
		return cache.invokeAll(keys, agent);
	}

	@SuppressWarnings("rawtypes")
	public Map getAll(Collection keys) {
		return cache.getAll(keys);
	}

	@SuppressWarnings("rawtypes")
	public Set entrySet(Filter filter, Comparator comparator) {
		return cache.entrySet(filter, comparator);
	}

	public String getCacheName() {
		return cache.getCacheName();
	}

	public void removeMapListener(MapListener listener,	 Object key) {
		cache.removeMapListener(listener, key);
	}

	@SuppressWarnings("rawtypes")
	public void addIndex(ValueExtractor attributeExtractor, boolean ordered, Comparator comparator) {
		cache.addIndex(attributeExtractor, ordered, comparator);
	}

	@SuppressWarnings("rawtypes")
	public Map invokeAll(Filter filter, EntryProcessor agent) {
		return cache.invokeAll(filter, agent);
	}

	public CacheService getCacheService() {
		return cache.getCacheService();
	}

	public boolean isActive() {
		return cache.isActive();
	}

	public void addMapListener(MapListener listener, Filter filter, boolean lite) {
		cache.addMapListener(listener, filter, lite);
	}

	@SuppressWarnings("rawtypes")
	public Object aggregate(Collection keys, EntryAggregator agent) {
		return cache.aggregate(keys, agent);
	}

	public void release() {
		cache.release();
	}

	public void removeIndex(ValueExtractor paramValueExtractor) {
		cache.removeIndex(paramValueExtractor);
	}

	public void destroy() {
		cache.destroy();
	}

	public Object put(Object paramObject1, Object paramObject2, long paramLong) {
		return cache.put(paramObject1, paramObject2, paramLong);
	}

	public void removeMapListener(MapListener paramMapListener,	 Filter paramFilter) {
		cache.removeMapListener(paramMapListener, paramFilter);
	}

	public Object aggregate(Filter paramFilter, EntryAggregator paramEntryAggregator) {
		return cache.aggregate(paramFilter, paramEntryAggregator);
	}

	public boolean lock(Object paramObject, long paramLong) {
		return cache.lock(paramObject, paramLong);
	}

	public boolean lock(Object paramObject) {
		return cache.lock(paramObject);
	}

	public boolean unlock(Object paramObject) {
		return cache.unlock(paramObject);
	}

	public int size() {
		return cache.size();
	}

	public boolean isEmpty() {
		return cache.isEmpty();
	}

	public boolean containsKey(Object key) {
		return cache.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return cache.containsValue(value);
	}

	public Object get(Object key) {
		return cache.get(key);
	}

	public Object put(Object key, Object value) {
		return cache.put(key, value);
	}

	public Object remove(Object key) {
		return cache.remove(key);
	}

	@SuppressWarnings("rawtypes")
	public void putAll(Map m) {
		cache.putAll(m);
	}

	public void clear() {
		cache.clear();
	}

	@SuppressWarnings("rawtypes")
	public Set keySet() {
		return cache.keySet();
	}

	@SuppressWarnings("rawtypes")
	public Collection values() {
		return cache.values();
	}

	@SuppressWarnings("rawtypes")
	public Set entrySet() {
		return cache.entrySet();
	}

	public boolean equals(Object o) {
		return cache.equals(o);
	}

	public int hashCode() {
		return cache.hashCode();
	}
}
