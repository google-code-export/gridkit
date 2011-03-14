package com.medx.framework.bean;

import java.util.Map;

import com.medx.framework.metadata.UserAttrKey;

public interface Bean {
	<V> V getAttribute(UserAttrKey<V> key);

	<V> void setAttribute(UserAttrKey<V> key, V value);
	
	<T> T cast(Class<T> clazz);
	
	Map<Integer, Object> asMap();
}
