package com.medx.framework.metadata;

import java.util.Set;

public interface TypeRegistry {
	<T> TypeKey<T> getTypeKey(int id);
	
	<T> TypeKey<T> getTypeKey(Class<T> clazz);
	
	Set<Integer> getTypeIds(Set<Integer> candidates);
}
