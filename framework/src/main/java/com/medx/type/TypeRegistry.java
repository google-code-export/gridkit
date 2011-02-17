package com.medx.type;

public interface TypeRegistry {
	Class<?> getType(int id);
	Class<?> getType(String name);
	
	int getTypeId(Class<?> clazz);
	String getTypeName(Class<?> clazz);
}
