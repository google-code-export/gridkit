package com.medx.framework.attribute;

public interface AttrMap {
	
	<V> V getAttribute(AttrKey<V> key);
	<V> void setAttribute(AttrKey<V> key, V value);
}
