package com.griddynamics.skeleton.attribute;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class AttributeKeyRegistry {
	private static final AttributeKeyRegistry instance = new AttributeKeyRegistry();
	
	private AttributeKeyRegistry() {}
	
	public static AttributeKeyRegistry getInstance() {
		return instance;
	}
	
	private ConcurrentMap<String, AttributeKey<?>> registryByString = new ConcurrentHashMap<String, AttributeKey<?>>();
	private ConcurrentMap<Integer, AttributeKey<?>> registryByInteger = new ConcurrentHashMap<Integer, AttributeKey<?>>();
	
	public void registerAttributeKey(String name, AttributeKey<?> attributeKey) {
		AttributeKey<?> pastAttribute = registryByString.putIfAbsent(name, attributeKey);
		
		if (pastAttribute != null)
			throw new RuntimeException("already registered name");
		
		pastAttribute = registryByInteger.putIfAbsent(attributeKey.getId(), attributeKey);
		
		if (pastAttribute != null)
			throw new RuntimeException("already registered id");
	}
	
	@SuppressWarnings("unchecked")
	public <T> AttributeKey<T> getAttributeKey(String name) {
		return (AttributeKey<T>) registryByString.get(name);
	}
	
	@SuppressWarnings("unchecked")
	public <T> AttributeKey<T> getAttributeKey(int id) {
		return (AttributeKey<T>) registryByInteger.get(id);
	}
}
