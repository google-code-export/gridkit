package com.medx.framework.type;

public interface TypeRegistry {
	<T> TypeKey<T> getTypeKey(int id);
	
	<T> TypeKey<T> getTypeKey(Class<T> clazz);
}
