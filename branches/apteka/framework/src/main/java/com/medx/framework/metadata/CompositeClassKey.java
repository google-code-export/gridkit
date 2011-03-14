package com.medx.framework.metadata;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class CompositeClassKey extends ClassKey {
	private static final Map<ClassKeyType, Class<?>> classKeyTypeMapping = new HashMap<ClassKeyType, Class<?>>();
	
	static {
		classKeyTypeMapping.put(ClassKeyType.MAP, Map.class);
		classKeyTypeMapping.put(ClassKeyType.SET, Set.class);
		classKeyTypeMapping.put(ClassKeyType.LIST, List.class);
		classKeyTypeMapping.put(ClassKeyType.COLLECTION, Collection.class);
	}
	
	private final ClassKey elementKey;
	
	CompositeClassKey(ClassKeyType type, ClassKey elementKey) {
		super(type);
		
		if (!classKeyTypeMapping.containsKey(type) && type != ClassKeyType.ARRAY)
			throw new IllegalArgumentException("type = " + type);
		
		if (elementKey.getType().isPrimitive() && type != ClassKeyType.ARRAY)
			throw new IllegalArgumentException("type = " + type + "; elementKey = " + elementKey);
		
		if (elementKey.getType().isEntry() && type != ClassKeyType.MAP)
			throw new IllegalArgumentException("type = " + type + "; elementKey = " + elementKey);
		
		this.elementKey = elementKey;
	}
	
	@Override
	public ClassKey getElementKey() {
		return elementKey;
	}
	
	@Override
	public Class<?> getJavaClass() {
		if (getType() != ClassKeyType.ARRAY)
			return classKeyTypeMapping.get(getType());
		else
			return Array.newInstance(elementKey.getJavaClass(), 0).getClass();
	}
}
