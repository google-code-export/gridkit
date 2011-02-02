package com.griddynamics.skeleton.attribute;

public interface AttributeMap {
	<V> V get(AttributeKey<V> key);
	
	<V> V set(AttributeKey<V> key, V value);
}
