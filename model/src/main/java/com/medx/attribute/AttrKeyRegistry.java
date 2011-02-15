package com.medx.attribute;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AttrKeyRegistry {
	private static final AttrKeyRegistry instance = new AttrKeyRegistry();
	
	private AttrKeyRegistry() {}
	
	public static AttrKeyRegistry getInstance() {
		return instance;
	}
	
	private ConcurrentMap<String, AttrKey<?>> registryByString = new ConcurrentHashMap<String, AttrKey<?>>();
	private ConcurrentMap<Integer, AttrKey<?>> registryByInteger = new ConcurrentHashMap<Integer, AttrKey<?>>();
	
	public void registerAttrKey(String name, AttrKey<?> attributeKey) {
		AttrKey<?> pastAttribute = registryByString.putIfAbsent(name, attributeKey);
		
		if (pastAttribute != null)
			throw new RuntimeException("already registered name");
		
		pastAttribute = registryByInteger.putIfAbsent(attributeKey.getId(), attributeKey);
		
		if (pastAttribute != null)
			throw new RuntimeException("already registered id");
	}
	
	@SuppressWarnings("unchecked")
	public <T> AttrKey<T> getAttrKey(String name) {
		return (AttrKey<T>) registryByString.get(name);
	}
	
	@SuppressWarnings("unchecked")
	public <T> AttrKey<T> getAttrKey(int id) {
		return (AttrKey<T>) registryByInteger.get(id);
	}
}
