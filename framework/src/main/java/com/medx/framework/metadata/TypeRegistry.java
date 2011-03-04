package com.medx.framework.metadata;

public interface TypeRegistry {
	<T> TypeKey<T> getTypeKey(int id);
	
	<T> TypeKey<T> getTypeKey(Class<T> clazz);
}
