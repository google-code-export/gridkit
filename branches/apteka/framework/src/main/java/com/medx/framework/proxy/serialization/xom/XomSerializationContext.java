package com.medx.framework.proxy.serialization.xom;

import java.util.Map;

public interface XomSerializationContext {
	<T> InternalXomSerializer<T> getXomSerializer(T object);
	
	Map<Object, Integer> getIdentityMap();
	
	int getNextObjectId();
	
	Map<Integer, Object> getObjectMap();
}
