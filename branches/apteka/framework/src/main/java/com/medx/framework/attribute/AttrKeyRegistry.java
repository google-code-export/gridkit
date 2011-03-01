package com.medx.framework.attribute;

public interface AttrKeyRegistry {
	<T> AttrKey<T> getAttrKey(int id);
	<T> AttrKey<T> getAttrKey(String name);
}
