/**
 * Copyright 2009 Grid Dynamics Consulting Services, Inc.
 */
package org.gridkit.coherence.offheap.storage;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;

public class ObjectStoreScheme extends AbstractMap<Object, Object> {
	
	private final ObjectStore store;	
	private final Set<Map.Entry<Object, Object>> entrySet = new EntrySet();
	private final boolean binaryMode;
	
	public ObjectStoreScheme(ObjectStore store, boolean binaryMode) {
	    this.store = store;
	    this.binaryMode = binaryMode;
	}

	protected ObjectStore getStore() {
	    return store;
	}
	
	@Override
	public Object get(Object key) {
	    if (binaryMode) {
	        key = toBinary(key);
	    }
		Object value = store.load(key);
		
		return binaryMode ? fromBinary(value) : value;
	}

	private Object fromBinary(Object bin) {
        return ExternalizableHelper.fromBinary((Binary) bin);
    }

    private Object toBinary(Object obj) {
        return obj == null ? null :  ExternalizableHelper.toBinary(obj);
    }

    @Override
	public boolean containsKey(Object key) {
	    if (binaryMode) {
	        key = toBinary(key);
	    }
		Object value = store.load(key);
		
		return value != null;
	}

	@Override
	public Object put(Object key, Object value) {
	    if (binaryMode) {
	        key = toBinary(key);
	        value = toBinary(value);
	    }
		Object old = store.load(key);
		store.store(key, value);		
		
		return binaryMode ? fromBinary(old) : old;
	}

    @Override
	public Object remove(Object key) {
	    if (binaryMode) {
	        key = toBinary(key);
	    }
		Object old = store.load(key);
		store.erase(key);		
		
		return binaryMode ? fromBinary(old) : old;
	}

	@Override
	public void clear() {
		store.eraseAll();
	}

	@Override
	public int size() {
		return getStore().size();
	}

	@Override
	public Set<Map.Entry<Object, Object>> entrySet() {
		return entrySet;
	}
	
    private class EntrySet extends AbstractCollection<Map.Entry<Object, Object>> implements Set<Map.Entry<Object, Object>>{

		@Override
		public Iterator<java.util.Map.Entry<Object, Object>> iterator() {			
			return new Iterator<Entry<Object,Object>>() {
				
				final Iterator<Object> keyIt = store.keys();
				boolean beforeStart = true;
				Object lastKey = null;
			
				@Override
				public void remove() {
					if (beforeStart) {
					    throw new IllegalStateException("Before start");
					}
					else {
					    store.erase(lastKey);
					}					
				}
			
				@Override
				public Entry<Object, Object> next() {
				    beforeStart = false;
					return new MapEntry(lastKey = keyIt.next());
				}
			
				@Override
				public boolean hasNext() {
					return keyIt.hasNext();
				}
			};
		}

		@Override
		public int size() {
			return store.size();
		}
	}
	
	private class MapEntry implements Map.Entry<Object, Object> {

		private final Object key;
		
		public MapEntry(Object key) {
			this.key = key;
		}

		@Override
		public Object getKey() {
			return binaryMode ? fromBinary(key) : key;
		}

		@Override
		public Object getValue() {
			Object value = store.load(key);
			return binaryMode ? fromBinary(value) : value;
		}

		@Override
		public Object setValue(Object value) {
			throw new UnsupportedOperationException();
		}		
	}
}
