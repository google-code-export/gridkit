package com.medx.framework.metadata;

public interface AttrMap {
	<V> V getAttribute(UserAttrKey<V> key);
	<V> void setAttribute(UserAttrKey<V> key, V value);
}
