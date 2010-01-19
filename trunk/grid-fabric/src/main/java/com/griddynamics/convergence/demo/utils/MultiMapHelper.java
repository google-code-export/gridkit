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
package com.griddynamics.convergence.demo.utils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Alexey Ragozin (aragozin@griddynamics.com)
 */
public class MultiMapHelper {

	public static <K> int addInteger(Map<K, ? super Integer> map, K key, int value) {
		Number num = (Number) map.get(key);
		if (num != null) {
			value += num.intValue();
		}
		map.put(key, new Integer(value));
		return value;
	}

	@SuppressWarnings("unchecked")
	public static <K, V> boolean addToList(Map<K, ? extends List<? super V>> map, K key, V value) {
		List<? super V> list = map.get(key);
		if (list == null) {
			((Map) map).put(key, list = new ArrayList(Collections.singleton(value)));
			return true;
		}
		else {
			return list.add(value);
		}
	}

	public static <K> boolean addToIntArray(Map<K, int[]> map, K key, int value) {
	    int[] list = map.get(key);	  
	    if (list == null) {
	        list = new int[]{value};
	    }
	    else {
	        list = Arrays.copyOf(list, list.length + 1);
	        list[list.length - 1] = value;
	    }
	    map.put(key, list);
	    return true;
	}

	public static <K> boolean addToLongArray(Map<K, long[]> map, K key, long value) {
	    long[] list = map.get(key);	  
	    if (list == null) {
	        list = new long[]{value};
	    }
	    else {
	        list = Arrays.copyOf(list, list.length + 1);
	        list[list.length - 1] = value;
	    }
	    map.put(key, list);
	    return true;
	}

	@SuppressWarnings("unchecked")
	public static <K, V> boolean addToSet(Map<K, ? extends Set<? super V>> map, K key, V value) {
		Set set = (Set) map.get(key);
		if (set == null) {
			((Map)map).put(key, set = new HashSet(Collections.singleton(value)));
			return true;
		}
		else {
			return set.add(value);
		}
	}

	@SuppressWarnings("unchecked")
	public static <K, V> boolean addToTreeSet(Map<K, ? extends Set<? super V>> map, K key, V value) {
		Set set = (Set) map.get(key);
		if (set == null) {
			((Map)map).put(key, set = new TreeSet<V>(Collections.singleton(value)));
			return true;
		}
		else {
			return set.add(value);
		}
	}

	@SuppressWarnings("unchecked")
	public static <K,V> boolean addToLinkedSet(Map<K, ? extends Set<? super V>> map, K key, V value) {
		Set set = (Set) map.get(key);
		if (set == null) {
			((Map)map).put(key, set = new LinkedHashSet(Collections.singleton(value)));
			return true;
		}
		else {
			return set.add(value);
		}
	}

	@SuppressWarnings("unchecked")
	public static <K1, K2, V> V putToLinkedMap(Map<K1, Map<K2, V>> map, K1 key, K2 subkey, V value) {
		Map submap = (Map) map.get(key);
		if (submap == null) {
			map.put(key, submap = new LinkedHashMap());
		}
		return (V)submap.put(subkey, value);
	}

	/**
	 * If key contains multiple equal objects (e.g. List). Only first fill be
	 * removed.
	 */
	@SuppressWarnings("unchecked")
	public static <K,V> boolean removeFromCollection(Map<K, ? extends Collection<? super V>> map, K key, V value) {
		Collection list = (Collection) map.get(key);
		if (list == null) {
			return false;
		}
		else {
			boolean result = list.remove(value);
			if (list.isEmpty()) {
				map.remove(key);
			}
			return result;
		}
	}

	@SuppressWarnings("unchecked")
	public static <K,V> boolean removeAllFromCollection(Map<K, ? extends Collection<? super V>> map, Object key, Collection<V> values) {
		Collection list = (Collection) map.get(key);
		if (list == null) {
			return false;
		}
		else {
			boolean result = list.removeAll(values);
			if (list.isEmpty()) {
				map.remove(key);
			}
			return result;
		}
	}
}
