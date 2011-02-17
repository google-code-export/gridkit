package com.medx.attribute;

public interface AttrMap {
	<V> V get(AttrKey<V> key);
	
	<V> void set(AttrKey<V> key, V value);
}
