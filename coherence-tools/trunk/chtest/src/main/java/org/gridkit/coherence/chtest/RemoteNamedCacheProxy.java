package org.gridkit.coherence.chtest;

import java.io.Serializable;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gridkit.zerormi.util.RemoteExporter;

import com.tangosol.net.CacheService;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.MapListener;
import com.tangosol.util.ValueExtractor;

/**
 * This is proxy for NamedCache interface.
 * It's purpose to sanitize API calls in assumption that {@link #delegate} 
 * is a remote proxy.
 *  
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class RemoteNamedCacheProxy implements NamedCache, Serializable {
	
	private static final long serialVersionUID = 20130921L;
	
	private RemoteExecContext hostContext;
	private NamedCache delegate;
	
	public RemoteNamedCacheProxy(final NamedCache cache) {
		this.hostContext = new RemoteExecContext() {
			@Override
			public <T> T exec(CacheCallable<T> task) {
				return task.call(cache);
			}
		};
		this.delegate = RemoteExporter.export(cache, NamedCache.class);
	}

	public void addIndex(ValueExtractor extractor, boolean sorted, Comparator order) {
		delegate.addIndex(extractor, sorted, order);
	}

	public void addMapListener(MapListener listener, Filter filter, boolean lite) {
		throw new UnsupportedOperationException();
	}

	public void addMapListener(MapListener listener, Object key, boolean lite) {
		throw new UnsupportedOperationException();
	}

	public void addMapListener(MapListener listener) {
		throw new UnsupportedOperationException();
	}

	public Object aggregate(Collection keys, EntryAggregator aggregator) {		
		return delegate.aggregate(new ArrayList(keys), aggregator);
	}

	public Object aggregate(Filter filter, EntryAggregator aggregator) {
		return delegate.aggregate(filter, aggregator);
	}

	public boolean containsKey(Object key) {
		return delegate.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return delegate.containsValue(value);
	}

	public void clear() {
		delegate.clear();
	}

	public void destroy() {
		delegate.destroy();
	}

	public Set entrySet() {
		return hostContext.exec(new CacheCallable<Map>() {
			@Override
			public Map call(NamedCache target) {
				return convertEntrySet(target.entrySet());
			}
		}).entrySet();
	}

	public Set entrySet(final Filter filter, final Comparator order) {
		return hostContext.exec(new CacheCallable<Map>() {
			@Override
			public Map call(NamedCache target) {
				return convertEntrySet(target.entrySet(filter, order));
			}
		}).entrySet();
	}

	public Set entrySet(final Filter filter) {
		return hostContext.exec(new CacheCallable<Map>() {
			@Override
			public Map call(NamedCache target) {
				return convertEntrySet(target.entrySet(filter));
			}
		}).entrySet();
	}

	public Object get(Object key) {
		return delegate.get(key);
	}

	public Map getAll(final Collection keys) {
		final List kk = new ArrayList(keys); 
		Map result = hostContext.exec(new CacheCallable<Map>() {
			@Override
			public Map call(NamedCache target) {
				return new LinkedHashMap(target.getAll(kk));
			}
		});
		return Collections.unmodifiableMap(result);
	}

	public String getCacheName() {
		return delegate.getCacheName();
	}

	public CacheService getCacheService() {
		throw new UnsupportedOperationException();
	}

	public Object invoke(Object key, EntryProcessor processor) {
		return delegate.invoke(key, processor);
	}

	public Map invokeAll(final Collection keys, final EntryProcessor processor) {
		final List kk = new ArrayList(keys); 
		Map result = hostContext.exec(new CacheCallable<Map>() {
			@Override
			public Map call(NamedCache target) {
				return new LinkedHashMap(target.invokeAll(kk, processor));
			}
		});
		return Collections.unmodifiableMap(result);
	}

	public Map invokeAll(final Filter filter, final EntryProcessor processor) {
		Map result = hostContext.exec(new CacheCallable<Map>() {
			@Override
			public Map call(NamedCache target) {
				return new LinkedHashMap(target.invokeAll(filter, processor));
			}
		});
		return Collections.unmodifiableMap(result);
	}

	public boolean isActive() {
		return delegate.isActive();
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	public Set keySet() {
		Set keySet = hostContext.exec(new CacheCallable<Set>() {
			@Override
			public Set call(NamedCache target) {
				return new LinkedHashSet(target.keySet());
			}
		});
		return Collections.unmodifiableSet(keySet);
	}

	public Set keySet(final Filter filter) {
		Set keySet = hostContext.exec(new CacheCallable<Set>() {
			@Override
			public Set call(NamedCache target) {
				return new LinkedHashSet(target.keySet(filter));
			}
		});
		return Collections.unmodifiableSet(keySet);
	}

	public boolean lock(Object key, long timeout) {
		return delegate.lock(key, timeout);
	}

	public boolean lock(Object key) {
		return delegate.lock(key);
	}

	public Object put(Object key, Object value, long expiry) {
		return delegate.put(key, value, expiry);
	}

	public Object put(Object key, Object value) {
		return delegate.put(key, value);
	}

	public void putAll(Map m) {
		delegate.putAll(m);
	}

	public void release() {
		delegate.release();
	}

	public Object remove(Object key) {
		return delegate.remove(key);
	}

	public void removeIndex(ValueExtractor extractor) {
		delegate.removeIndex(extractor);
	}

	public void removeMapListener(MapListener listener, Filter filter) {
		throw new UnsupportedOperationException();
	}

	public void removeMapListener(MapListener listener, Object key) {
		throw new UnsupportedOperationException();
	}

	public void removeMapListener(MapListener listener) {
		throw new UnsupportedOperationException();
	}

	public int size() {
		return delegate.size();
	}

	public boolean unlock(Object key) {
		return delegate.unlock(key);
	}

	public Collection values() {
		Collection values = hostContext.exec(new CacheCallable<Collection>() {
			@Override
			public Collection call(NamedCache target) {
				return new ArrayList(target.values());
			}
		});
		return Collections.unmodifiableCollection(values);
	}

	private static Map convertEntrySet(Set entrySet) {
		Map map = new LinkedHashMap();
		for(Object e: entrySet) {
			Map.Entry entry = (Map.Entry) e;
			map.put(entry.getKey(), entry.getValue());
		}
		return Collections.unmodifiableMap(map);
	}
		
	@Override
	public String toString() {
		return "RemoteNamedCacheProxy[" + getCacheName() + "]";
	}
	
	private static interface RemoteExecContext extends Remote {
		
		public <T> T exec(CacheCallable<T> task);
		
	}
	
	private static interface CacheCallable<T> {
		
		public T call(NamedCache cache);
		
	}
}
