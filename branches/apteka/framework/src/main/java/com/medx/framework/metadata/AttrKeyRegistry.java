package com.medx.framework.metadata;

import com.medx.framework.attribute.AttrKey;

public interface AttrKeyRegistry {
	
	<T> AttrKey<T> getAttrKey(int id);
	<T> AttrKey<T> getAttrKey(String name);
	
}
