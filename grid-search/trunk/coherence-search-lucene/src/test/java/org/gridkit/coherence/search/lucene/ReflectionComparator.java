package org.gridkit.coherence.search.lucene;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;

/**
 * A test utility for deep equivalence comparison of objects.
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@Ignore
public class ReflectionComparator {
	
	public boolean equivalent(Object o1, Object o2) throws IllegalArgumentException, IllegalAccessException {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null || o2 == null) {
			return false;
		}
		if (o1 instanceof Collection<?> && o2 instanceof Collection<?>) {
			return compareCollections(o1, o2);
		}
		if (o1 instanceof Map<?, ?> && o2 instanceof Map<?, ?>) {
			return compareMaps((Map<?, ?>)o1, (Map<?, ?>)o2);
		}
		if (o1.getClass().isArray() && o2.getClass().isArray()) {
			return compareArrays(o1, o2);
		}
		
		if (o1.getClass() == String.class && o2.getClass() == String.class) {
			return o1.equals(o2);
		}
		if (o1.getClass() == Boolean.class && o2.getClass() == Boolean.class) {
			return o1.equals(o2);
		}
		if (o1 instanceof Number && o2 instanceof Number) {
			return o1.equals(o2);
		}
		if (o1.getClass() != o2.getClass()) {
			return false;
		}
		else {
			return compareFields(o1, o2);
		}
	}

	private boolean compareCollections(Object o1, Object o2) throws IllegalArgumentException, IllegalAccessException {
		if (o1 instanceof Set<?> && o2 instanceof Set<?>) {
			return compareSets((Set<?>)o1, (Set<?>)o2);
		}
		else {
			return compareOrderedCollections((Collection<?>)o1, (Collection<?>) o2);
		}
	}
	
	private boolean compareOrderedCollections(Collection<?> o1, Collection<?> o2) throws IllegalArgumentException, IllegalAccessException {
		if (o1.size() != o2.size()) {
			return false;
		};
		Iterator<?> it1 = o1.iterator();
		Iterator<?> it2 = o1.iterator();
		while(it1.hasNext() && it2.hasNext()) {
			Object e1 = it1.next();
			Object e2 = it2.next();
			
			if (!equivalent(e1, e2)) {
				return false; 
			}
		}
		
		return (!it1.hasNext()) && (!it2.hasNext());
	}


	private boolean compareSets(Set<?> o1, Set<?> o2) throws IllegalArgumentException, IllegalAccessException {
		if (o1.size() != o2.size()) {
			return false;
		};
		List<Object> l1 = new ArrayList<Object>(o1);
		List<Object> l2 = new ArrayList<Object>(o2);
		
		loop1:
		for(Object e1 : l1) {
			Iterator<?> it = l2.iterator();
			while(it.hasNext()){
				Object e2 = it.next();
				if (equivalent(e1, e2)) {
					it.remove();
					continue loop1;
				}
			}
			return false;
		}
		
		return true;
	}

	private boolean compareMaps(Map<?, ?> o1, Map<?, ?> o2) {
		if (o1.size() != o2.size()) {
			return false;
		};
		
		for(Map.Entry<?, ?> e1 : o1.entrySet()) {
			Object k1 = e1.getKey();
			Object v1 = e1.getValue();
			if (!o2.containsKey(k1)) {
				return false;
			}
			else {
				Object v2 = o2.get(k1);
				if (!v1.equals(v2)) {
					return false;
				}
			}
		}
		
		return true;
	}

	private boolean compareArrays(Object o1, Object o2) throws IllegalArgumentException, IllegalAccessException {
		if (o1 instanceof Object[] && o2 instanceof Object[]) {
			return compareOrderedCollections(Arrays.asList((Object[])o1), Arrays.asList((Object[])o2));
		}
		else {
			if (Array.getLength(o1) != Array.getLength(o2)) {
				return false;
			}
			else {
				for(int i = 0; i != Array.getLength(o1); i++) {
					Object e1 = Array.get(o1, i);
					Object e2 = Array.get(o2, i);
					if (!e1.equals(e2)) {
						return false;
					}
				}
			}
			return true;
		}
	}

	private boolean compareFields(Object o1, Object o2) throws IllegalArgumentException, IllegalAccessException {
		Class<?> t = o1.getClass();
		while(t != Object.class) {
			if (!compareFields(t, o1, o2)) {
				return false;
			}
			t = t.getSuperclass();
		}
		return true;
	}

	private boolean compareFields(Class<?> t, Object o1, Object o2) throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = t.getDeclaredFields();
		for(Field field : fields) {
			if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
				field.setAccessible(true);
				
				Object e1 = field.get(o1);
				Object e2 = field.get(o2);
				
				if (! equivalent(e1, e2)) {
					return false;
				}
			}
		}
		return true;
	}
}
