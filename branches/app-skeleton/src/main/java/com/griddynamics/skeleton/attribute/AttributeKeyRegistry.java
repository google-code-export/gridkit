package com.griddynamics.skeleton.attribute;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AttributeKeyRegistry {
	private static final AttributeKeyRegistry instance = new AttributeKeyRegistry(new ConcurrentHashMap<Short, AttributeKey<?>>());
	
	private Map<Short, AttributeKey<?>> registry;
	
	private AttributeKeyRegistry(Map<Short, AttributeKey<?>> registry) {
		this.registry = registry;
	}
	
	@SuppressWarnings("unchecked")
	public <T> AttributeKey<T> getAttributeKey(short id) {
		return (AttributeKey<T>)registry.get(id);
	}
}
