/**
 * Copyright 2008-2009 Grid Dynamics Consulting Services, Inc.
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
package com.griddynamics.gridkit.collections.map;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.griddynamics.gridkit.collections.ObjectStore;

/**
 * Implements {@link Map} interface using provided {@link ObjectStore}
 * 
 * @author Alexey Ragozin (aragozin@gridsynamics.com)
 */
public class StoreToMapAdapter extends AbstractMap<Object, Object> {
	
	private final ObjectStore store;	
	private final Set<Map.Entry<Object, Object>> entrySet = new EntrySet();
	
	public StoreToMapAdapter(ObjectStore store) {
	    this.store = store;
	}

	protected ObjectStore getStore() {
	    return store;
	}
	
	@Override
	public Object get(Object key) {
		Object value = store.load(key);		
		return value;
	}

    @Override
	public boolean containsKey(Object key) {
		Object value = store.load(key);
		return value != null;
	}

	@Override
	public Object put(Object key, Object value) {
		Object old = store.load(key);
		store.store(key, value);				
		return old;
	}

    @Override
	public Object remove(Object key) {
		Object old = store.load(key);
		store.erase(key);				
		return old;
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
			return key;
		}

		@Override
		public Object getValue() {
			Object value = store.load(key);
			return value;
		}

		@Override
		public Object setValue(Object value) {
			throw new UnsupportedOperationException();
		}		
	}
}
