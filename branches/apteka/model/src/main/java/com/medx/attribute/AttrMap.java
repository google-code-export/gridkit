package com.medx.attribute;

public interface AttrMap {
	<V> V get(AttrKey<V> key);
	
	<V> V set(AttrKey<V> key, V value);
}
