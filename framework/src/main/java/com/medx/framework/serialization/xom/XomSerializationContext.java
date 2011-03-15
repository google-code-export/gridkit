package com.medx.framework.serialization.xom;

import java.util.Map;

import nu.xom.Element;

public interface XomSerializationContext {
	<T> InternalXomSerializer<T> getXomSerializer(T object);
	
	<T> InternalXomSerializer<T> getXomSerializer(Element element);
	
	Map<Object, Integer> getIdentityMap();
	
	int getNextObjectId();
	
	Map<Integer, Object> getObjectMap();
}
