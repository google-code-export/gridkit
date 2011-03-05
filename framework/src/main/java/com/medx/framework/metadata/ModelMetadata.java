package com.medx.framework.metadata;

import java.util.Set;

public interface ModelMetadata {
	<T> AttrKey<T> getAttrKey(int id);
	<T> AttrKey<T> getAttrKey(String name);
	
	<T> TypeKey<T> getTypeKey(int id);
	<T> TypeKey<T> getTypeKey(Class<T> clazz);
	
	Set<Integer> getTypeIds(Set<Integer> candidates);
}
