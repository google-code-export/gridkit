/**
 * Copyright 2013 Alexey Ragozin
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
package org.gridkit.coherence.test.rwbm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.tangosol.net.cache.CacheStore;

public class LoadAllCacheStore implements CacheStore {

	public LoadAllCacheStore() {
		System.out.println("SimpleCacheStore created");
	}
	
	@Override
	public Object load(Object key) {		
		System.out.println("load:" + key);
		return key;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map loadAll(Collection keys) {
		System.out.println("loadAll: " + keys);
		Map result = new HashMap();
		for(Object key: keys) {
			if ("ALL".equals(key)) {
				keys.clear();
				keys.add("A");
				keys.add("B");
				keys.add("C");
				keys.add("D");
				result.clear();
				result.put("A", "aaa");
				result.put("B", "bbb");
				result.put("C", "ccc");
				result.put("D", "ddd");
				return result;
			}
			else {
				result.put(key, key);
			}			
		}
		return result;
	}

	@Override
	public void store(Object key, Object value) {
		System.out.println(key + " -> " + value);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void storeAll(Map paramMap) {
	}

	@Override
	public void erase(Object key) {
		System.out.println("erase: " + key);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void eraseAll(Collection paramCollection) {
	}
}
