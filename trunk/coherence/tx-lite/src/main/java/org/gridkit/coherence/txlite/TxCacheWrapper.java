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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tangosol.net.CacheService;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.MapListener;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.IdentityExtractor;
import com.tangosol.util.filter.AlwaysFilter;
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
	public TxSession getSession() {
		return (TxSession) (adapter instanceof TxSession ? adapter : null);
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
		nestedCache.invokeAll(new HashSet(m.keySet()), adapter.newPutProcessor(this, new HashMap(m)));
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
		Set keys = new HashSet(nestedCache.keySet());
		return new WrapperSetView(keys) {
						
			@Override
			protected void delete(Object element) {
				remove(element);
				delegate.remove(element);
			}
			
			@Override
			protected void deleteAll(Collection elements) {
				removeAll(elements);
				delegate.removeAll(elements);
			}
		};
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set keySet(Filter filter) {
		adapter.beforeOperation(this);
		Filter transformed = adapter.transformFilter(this, filter);
		Set keys = new HashSet(nestedCache.keySet(transformed));
		WrapperSetView wrapperSetView = new WrapperSetView(keys) {
						
			@Override
			protected void delete(Object element) {
				remove(element);
				delegate.remove(element);
			}
			
			@Override
			protected void deleteAll(Collection elements) {
				removeAll(elements);
				delegate.removeAll(elements);
			}
		};
		adapter.afterOperation(this);
		return wrapperSetView;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set entrySet() {
		return entrySet(AlwaysFilter.INSTANCE);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set entrySet(Filter filter) {
		adapter.beforeOperation(this);
		Filter transformedFilter = adapter.transformFilter(this, filter);
		Set entries = new HashSet(nestedCache.entrySet(transformedFilter));
		Set result = new HashSet(entries.size());
		for(Object x: entries) {
			Map.Entry centry = (Map.Entry) x;
			result.add(new EntryWrapper(centry.getKey(), adapter.transformValue(this, (ValueContatiner) centry.getValue())));
		}
		
		WrapperSetView wrapperSetView = new WrapperSetView(result) {
						
			@Override
			protected void delete(Object element) {
				remove(((Map.Entry)element).getKey());
				delegate.remove(element);
			}
			
			@Override
			protected void deleteAll(Collection elements) {
				List keys = new ArrayList();
				for (Object e : elements) {
					keys.add(((Map.Entry)e).getKey());
				}
				removeAll(keys);
				delegate.removeAll(elements);
			}
		};
		adapter.afterOperation(this);
		return wrapperSetView;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Set entrySet(Filter filter, final Comparator comparator) {
		adapter.beforeOperation(this);
		Filter transformedFilter = adapter.transformFilter(this, filter);
		Set entries = new HashSet(nestedCache.entrySet(transformedFilter));
		List result = new ArrayList(entries.size());
		for(Object x: entries) {
			Map.Entry centry = (Map.Entry) x;
			result.add(new EntryWrapper(centry.getKey(), adapter.transformValue(this, (ValueContatiner) centry.getValue())));
		}
		
		Collections.sort(result, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				o1 = ((Map.Entry)o1).getValue();
				o2 = ((Map.Entry)o2).getValue();
				if (comparator == null) {
					return ((Comparable)o1).compareTo(o2);
				}
				else {
					return comparator.compare(o1, o2);
				}
			}
		}); 
		
		WrapperSetView wrapperSetView = new WrapperSetView(result) {
						
			@Override
			protected void delete(Object element) {
				remove(((Map.Entry)element).getKey());
				delegate.remove(element);
			}
			
			@Override
			protected void deleteAll(Collection elements) {
				List keys = new ArrayList();
				for (Object e : elements) {
					keys.add(((Map.Entry)e).getKey());
				}
				removeAll(keys);
				delegate.removeAll(elements);
			}
		};
		adapter.afterOperation(this);
		return wrapperSetView;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection values() {
		adapter.beforeOperation(this);
		Collection values = nestedCache.values();
		List result = new ArrayList(values.size());
		for(Object value : values) {
			Object cv = adapter.transformValue(this, (ValueContatiner) value);
			if (cv != null) {
				result.add(cv);
			}
		}		
		adapter.afterOperation(this);
		return result;
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
	
	@SuppressWarnings("unchecked")
	private abstract static class WrapperSetView implements Set {

		protected Collection delegate;
	
		public WrapperSetView(Collection delegate) {
			this.delegate = delegate;
		}
		
		protected boolean isConverted() {
			return false;
		}
		
		protected Object convert(Object element) {
			return element;
		}
		
		protected abstract void delete(Object element);

		protected abstract void deleteAll(Collection element);

		@Override
		public boolean add(Object e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection c) {
			if (!c.isEmpty()) {
				throw new UnsupportedOperationException();
			}
			else {
				return false;
			}
		}

		@Override
		public void clear() {
			if (!isConverted()) {
				deleteAll(delegate);
			}
			else {
				deleteAll(Arrays.asList(toArray()));
			}
		}

		@Override
		public boolean contains(Object o) {
			if (o == null) {
				return false;
			}
			else if (!isConverted()) {
				return delegate.contains(o);
			}
			else {
				for(Object obj : delegate) {
					if (o.equals(convert(obj))) {
						return true;
					}
				}
				return false;
			}
		}

		@Override
		public boolean containsAll(Collection c) {
			for(Object o : c) {
				if (!contains(o)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean isEmpty() {
			return delegate.isEmpty();
		}

		@Override
		public Iterator iterator() {
			final Iterator nested = delegate.iterator();
			return new Iterator() {
				
				Object last = null;
				
				@Override
				public boolean hasNext() {
					return nested.hasNext();
				}

				@Override
				public Object next() {
					last = nested.next();
					return convert(last);
				}

				@Override
				public void remove() {
					if (last == null) {
						throw new IllegalStateException();
					}
					else {
						delete(convert(last));
					}
				}
			};
		}

		@Override
		public boolean remove(Object o) {
			boolean contains = contains(o);
			delete(o);
			return contains;
		}

		@Override
		public boolean removeAll(Collection c) {
			int size = delegate.size();
			deleteAll(c);
			return size != delegate.size();
		}

		@Override
		public boolean retainAll(Collection c) {
			List forRemoval = new ArrayList(); 
			for(Object x : delegate) {
				Object y = convert(x);
				if (!c.contains(y)) {
					forRemoval.add(y);
				}
			}
			deleteAll(forRemoval);
			return forRemoval.size() > 0;
		}

		@Override
		public int size() {
			return delegate.size();
		}

		@Override
		public Object[] toArray() {
			return toArray(new Object[size()]);
		}

		@Override
		public Object[] toArray(Object[] a) {
			if (a.length < size()) {
				a = Arrays.copyOf(a, size());
			}
			int n = 0;
			for(Object x : delegate) {
				Object y = convert(x);
				a[n] = y;
				++n;
			}
			return a;
		}
		
		@Override
		public String toString() {
			return Arrays.toString(toArray());
		}
	}
	
	@SuppressWarnings("unchecked")
	private class EntryWrapper implements Map.Entry {

		private Object key;
		private Object value;

		public EntryWrapper(Object key, Object value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public Object getKey() {
			return key;
		}

		@Override
		public Object getValue() {
			return value;
		}

		@Override
		public Object setValue(Object value) {
			Object old = put(key, value);
			this.value = get(key);
			return old;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EntryWrapper other = (EntryWrapper) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			return true;
		}

		private TxCacheWrapper getOuterType() {
			return TxCacheWrapper.this;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(key).append("->").append(value);
			return builder.toString();
		}
	}
}
