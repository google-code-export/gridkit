package com.griddynamics.coherence.integration.spring;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import com.griddynamics.coherence.integration.spring.config.CacheDefinition;
import com.tangosol.net.CacheService;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.MapListener;
import com.tangosol.util.ValueExtractor;

/**
 * Proxy for the proper NamedCache initialization in Spring container
 * TODO: document the necessity of this proxy
 * @author Dmitri Babaev
 */
@SuppressWarnings("unchecked")
public class LazyNamedCache implements NamedCache {
	NamedCache inner;
	CacheDefinition cacheDefinition;
	
	public LazyNamedCache(CacheDefinition cacheDefinition) {
		this.cacheDefinition = cacheDefinition;
	}
	
	private NamedCache getInnerCache() {
		if (inner == null)
			inner = cacheDefinition.newCache();
		
		return inner;
	}

	public void addIndex(ValueExtractor paramValueExtractor,
			boolean paramBoolean, Comparator paramComparator) {
		getInnerCache().addIndex(paramValueExtractor, paramBoolean, paramComparator);
	}

	public void addMapListener(MapListener paramMapListener,
			Filter paramFilter, boolean paramBoolean) {
		getInnerCache().addMapListener(paramMapListener, paramFilter, paramBoolean);
	}

	public void addMapListener(MapListener paramMapListener,
			Object paramObject, boolean paramBoolean) {
		getInnerCache().addMapListener(paramMapListener, paramObject, paramBoolean);
	}

	public void addMapListener(MapListener paramMapListener) {
		getInnerCache().addMapListener(paramMapListener);
	}

	public Object aggregate(Collection paramCollection,
			EntryAggregator paramEntryAggregator) {
		return getInnerCache().aggregate(paramCollection, paramEntryAggregator);
	}

	public Object aggregate(Filter paramFilter,
			EntryAggregator paramEntryAggregator) {
		return getInnerCache().aggregate(paramFilter, paramEntryAggregator);
	}

	public void clear() {
		getInnerCache().clear();
	}

	public boolean containsKey(Object key) {
		return getInnerCache().containsKey(key);
	}

	public boolean containsValue(Object value) {
		return getInnerCache().containsValue(value);
	}

	public void destroy() {
		getInnerCache().destroy();
	}

	public Set entrySet() {
		return getInnerCache().entrySet();
	}

	public Set entrySet(Filter paramFilter, Comparator paramComparator) {
		return getInnerCache().entrySet(paramFilter, paramComparator);
	}

	public Set entrySet(Filter paramFilter) {
		return getInnerCache().entrySet(paramFilter);
	}

	public boolean equals(Object o) {
		return getInnerCache().equals(o);
	}

	public Object get(Object key) {
		return getInnerCache().get(key);
	}

	public Map getAll(Collection paramCollection) {
		return getInnerCache().getAll(paramCollection);
	}

	public String getCacheName() {
		return getInnerCache().getCacheName();
	}

	public CacheService getCacheService() {
		return getInnerCache().getCacheService();
	}

	public int hashCode() {
		return getInnerCache().hashCode();
	}

	public Object invoke(Object paramObject, EntryProcessor paramEntryProcessor) {
		return getInnerCache().invoke(paramObject, paramEntryProcessor);
	}

	public Map invokeAll(Collection paramCollection,
			EntryProcessor paramEntryProcessor) {
		return getInnerCache().invokeAll(paramCollection, paramEntryProcessor);
	}

	public Map invokeAll(Filter paramFilter, EntryProcessor paramEntryProcessor) {
		return getInnerCache().invokeAll(paramFilter, paramEntryProcessor);
	}

	public boolean isActive() {
		return getInnerCache().isActive();
	}

	public boolean isEmpty() {
		return getInnerCache().isEmpty();
	}

	public Set keySet() {
		return getInnerCache().keySet();
	}

	public Set keySet(Filter paramFilter) {
		return getInnerCache().keySet(paramFilter);
	}

	public boolean lock(Object paramObject, long paramLong) {
		return getInnerCache().lock(paramObject, paramLong);
	}

	public boolean lock(Object paramObject) {
		return getInnerCache().lock(paramObject);
	}

	public Object put(Object paramObject1, Object paramObject2, long paramLong) {
		return getInnerCache().put(paramObject1, paramObject2, paramLong);
	}

	public Object put(Object key, Object value) {
		return getInnerCache().put(key, value);
	}

	public void putAll(Map m) {
		getInnerCache().putAll(m);
	}

	public void release() {
		getInnerCache().release();
	}

	public Object remove(Object key) {
		return getInnerCache().remove(key);
	}

	public void removeIndex(ValueExtractor paramValueExtractor) {
		getInnerCache().removeIndex(paramValueExtractor);
	}

	public void removeMapListener(MapListener paramMapListener,
			Filter paramFilter) {
		getInnerCache().removeMapListener(paramMapListener, paramFilter);
	}

	public void removeMapListener(MapListener paramMapListener,
			Object paramObject) {
		getInnerCache().removeMapListener(paramMapListener, paramObject);
	}

	public void removeMapListener(MapListener paramMapListener) {
		getInnerCache().removeMapListener(paramMapListener);
	}

	public int size() {
		return getInnerCache().size();
	}

	public boolean unlock(Object paramObject) {
		return getInnerCache().unlock(paramObject);
	}

	public Collection values() {
		return getInnerCache().values();
	}
}
