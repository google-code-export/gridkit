/**
 * Copyright 2011 Grid Dynamics Consulting Services, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gridkit.coherence.txlite;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.tangosol.net.CacheService;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.MapListener;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.IdentityExtractor;
import com.tangosol.util.filter.EqualsFilter;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@SuppressWarnings("deprecation")
class TxCacheWrapper implements NamedCache, TxWrappedCache {

	private NamedCache nestedCache;
	private CacheAccessAdapter adapter;
	
	public TxCacheWrapper(NamedCache nestedCache, CacheAccessAdapter adapter) {
		this.nestedCache = nestedCache;
		this.adapter = adapter;
	}
	
	@Override
	public String getCacheName() {
		return nestedCache.getCacheName();
	}
	
	public NamedCache getVersionedCache() {
		return nestedCache;
	}

	@Override
	public CacheService getCacheService() {
		return nestedCache.getCacheService();
	}

	@Override
	public boolean isActive() {
		return nestedCache.isActive();
	}

	/**
	 * Transactionally inaccurate, may return true even is key is invisible
	 * to a transaction.
	 */
	@Override
	public boolean containsKey(Object key) {
		return nestedCache.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		// brute force search
		return !keySet(new EqualsFilter(IdentityExtractor.INSTANCE, value)).isEmpty();
	}

	@Override
	public Object get(Object oKey) {
		adapter.beforeOperation(this);
		Object result = internalGet(oKey);
		adapter.afterOperation(this);
		return result;
	}

	protected Object internalGet(Object oKey) {
		ValueContatiner vc = (ValueContatiner) nestedCache.get(oKey);
		Object result = vc == null ? null : adapter.transformValue(this, vc);
		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map getAll(Collection keys) {
		adapter.beforeOperation(this);
		Map vclist = nestedCache.getAll(keys);
		Map result = new HashMap();
		for(Map.Entry entry: (Collection<Map.Entry>)vclist.entrySet()) {
			Object key = entry.getKey();
			ValueContatiner vc = (ValueContatiner) entry.getValue();
			Object value = vc == null ? null : adapter.transformValue(this, vc);
			if (value != null) {
				result.put(key, value);
			}
		}
		adapter.afterOperation(this);
		return result;
	}

	@Override
	public Object put(Object key, Object value) {
		if (adapter.isReadOnly(this)) {
			throw new UnsupportedOperationException("Read only mode");
		}
		adapter.beforeOperation(this);
		// TODO not efficient but functional
		Object oldValue = internalGet(key);
		adapter.markDirty(this, key);
		nestedCache.invoke(key, adapter.newPutProcessor(this, key, value));
		adapter.afterOperation(this);
		return oldValue;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void putAll(Map m) {
		if (adapter.isReadOnly(this)) {
			throw new UnsupportedOperationException("Read only mode");
		}
		adapter.beforeOperation(this);
		// TODO not efficient but functional
		adapter.markDirty(this, m.keySet());
		nestedCache.invoke(m.keySet(), adapter.newPutProcessor(this, new HashMap(m)));
		adapter.afterOperation(this);
	}

	@Override
	public Object put(Object key, Object value, long expiry) {
		throw new UnsupportedOperationException("Expiry is not supported by TxLite cache");
	}

	@Override
	public Object remove(Object key) {
		if (adapter.isReadOnly(this)) {
			throw new UnsupportedOperationException("Read only mode");
		}
		return put(key, null); // null is equivalent to no value
	}

	/**
	 * Transactionally inaccurate, may return true even is key is invisible
	 * to a transaction.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Set keySet() {
		// view semantic handler
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set keySet(Filter paramFilter) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set entrySet() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set entrySet(Filter filter) {
		// TODO
		throw new UnsupportedOperationException();
//		Filter transformedFilter = adapter.transformFilter(filter);
//		return super.entrySet(filter);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Set entrySet(Filter filter, Comparator comparator) {
		// TODO
		throw new UnsupportedOperationException();
//		return super.entrySet(filter, comparator);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection values() {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object aggregate(Collection collKeys, EntryAggregator agent) {
		adapter.beforeOperation(this);
		EntryAggregator transformedAgent = adapter.transformAggregator(this, agent);
		Object result = nestedCache.aggregate(collKeys, transformedAgent);
		adapter.afterOperation(this);
		return result;
	}

	@Override
	public Object aggregate(Filter filter, EntryAggregator agent) {
		adapter.beforeOperation(this);
		Filter transformedFilter = adapter.transformFilter(this, filter);
		EntryAggregator transformedAgent = adapter.transformAggregator(this, agent);
		Object result = nestedCache.aggregate(transformedFilter, transformedAgent);
		adapter.afterOperation(this);
		return result;
	}

	@Override
	public Object invoke(Object key, EntryProcessor agent) {
		if (adapter.isReadOnly(this)) {
			adapter.beforeOperation(this);
			EntryProcessor transformedAgent = adapter.transformProcessor(this, agent);
			Object result = nestedCache.invoke(key, transformedAgent);
			adapter.afterOperation(this);
			return result;
		}
		else {
			adapter.beforeOperation(this);
			EntryProcessor transformedAgent = adapter.transformProcessor(this, agent);
			adapter.markDirty(this, key);
			Object result = nestedCache.invoke(key, transformedAgent);
			adapter.afterOperation(this);
			return result;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map invokeAll(Collection keys, EntryProcessor agent) {
		if (adapter.isReadOnly(this)) {
			adapter.beforeOperation(this);
			EntryProcessor transformedAgent = adapter.transformProcessor(this, agent);
			Map result = nestedCache.invokeAll(keys, transformedAgent);
			adapter.afterOperation(this);
			return result;
		}
		else {
			adapter.beforeOperation(this);
			EntryProcessor transformedAgent = adapter.transformProcessor(this, agent);
			adapter.markDirty(this, keys);
			Map result = nestedCache.invokeAll(keys, transformedAgent);
			adapter.afterOperation(this);
			return result;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map invokeAll(Filter filter, EntryProcessor agent) {
		if (adapter.isReadOnly(this)) {
			adapter.beforeOperation(this);
			EntryProcessor transformedAgent = adapter.transformProcessor(this, agent);
			Filter transformedFilter = adapter.transformFilter(this, filter);
			Map result = nestedCache.invokeAll(transformedFilter, transformedAgent);
			adapter.afterOperation(this);
			return result;
		}
		else {
			adapter.beforeOperation(this);
			EntryProcessor transformedAgent = adapter.transformProcessor(this, agent);
			Filter transformedFilter = adapter.transformFilter(this, filter);
			// all keys affected by processor should be marked as dirty in transaction
			Collection keys = nestedCache.keySet(transformedFilter);
			adapter.markDirty(this, keys);
			Map result = nestedCache.invokeAll(keys, transformedAgent);
			adapter.afterOperation(this);
			return result;
		}
	}

	/**
	 * Transactionally inaccurate, may count keys not visible for current transaction.
	 */
	@Override
	public boolean isEmpty() {
		return nestedCache.isEmpty();
	}

	/**
	 * Transactionally inaccurate, may count keys not visible for current transaction.
	 */
	@Override
	public int size() {
		return nestedCache.size();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean lock(Object key) {
		return nestedCache.lock(key);
	}
	
	@Override
	public boolean lock(Object key, long timeout) {
		return nestedCache.lock(key, timeout);
	}

	@Override
	public boolean unlock(Object key) {
		return nestedCache.unlock(key);
	}

	@Override
	public void destroy() {
		nestedCache.release();		
	}

	@Override
	public void release() {
		nestedCache.release();		
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addIndex(ValueExtractor extractor, boolean fOrdered, Comparator comparator) {
		// does not count as cache operation
		ValueExtractor transformed = adapter.transformIndexExtractor(this, extractor);
		nestedCache.addIndex(transformed, fOrdered, comparator);
	}

	@Override
	public void removeIndex(ValueExtractor extractor) {
		// does not count as cache operation
		ValueExtractor transformed = adapter.transformIndexExtractor(this, extractor);
		nestedCache.removeIndex(transformed);
	}

	@Override
	public void addMapListener(MapListener listener) {
		MapListener transformend = adapter.transformListener(this, listener);
		nestedCache.addMapListener(transformend);
	}

	@Override
	public void addMapListener(MapListener listener, Object key, boolean lite) {
		MapListener transformend = adapter.transformListener(this, listener);
		nestedCache.addMapListener(transformend, key, true);
	}

	@Override
	public void addMapListener(MapListener listener, Filter filter, boolean lite) {
		MapListener transformend = adapter.transformListener(this, listener);
		Filter transformedFilter = adapter.transformListenerFilter(this, filter);
		nestedCache.addMapListener(transformend, transformedFilter, true);
	}

	@Override
	public void removeMapListener(MapListener listener) {
		MapListener transformend = adapter.transformListener(this, listener);
		nestedCache.removeMapListener(transformend);
	}

	@Override
	public void removeMapListener(MapListener listener,	Object key) {
		MapListener transformend = adapter.transformListener(this, listener);
		nestedCache.removeMapListener(transformend, key);
	}

	@Override
	public void removeMapListener(MapListener listener,	Filter filter) {
		MapListener transformend = adapter.transformListener(this, listener);
		Filter transformedFilter = adapter.transformListenerFilter(this, filter);
		nestedCache.removeMapListener(transformend, transformedFilter);
	}
}
