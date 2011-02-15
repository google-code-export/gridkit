package com.medx.type;

public interface TypeRegistry {
	Class<?> getType(int id);
	int getTypeId(Class<?> clazz);
}
