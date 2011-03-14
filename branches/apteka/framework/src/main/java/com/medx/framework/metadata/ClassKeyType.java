package com.medx.framework.metadata;

public enum ClassKeyType {
	PRIMITIVE, STANDARD, ENUM, ENTRY, USER, ARRAY, COLLECTION, LIST, SET, MAP;
	
	public boolean isPrimitiveOrEntry() {
		return this == PRIMITIVE || this == ENTRY;
	}
	
	public boolean isPrimitive() {
		return this == PRIMITIVE;
	}
	
	public boolean isEntry() {
		return this == ENTRY;
	}
}
