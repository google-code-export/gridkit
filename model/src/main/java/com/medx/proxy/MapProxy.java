package com.medx.proxy;

import java.util.Map;

public interface MapProxy {
	<T> T cast(Class<T> clazz);
	
	Object getAttributeValue(int attributeId);
	
	void setAttributeValue(int attributeId, Object value);
	
	Map<Integer, Object> getBackendMap();
	
	Map<String, Object> exportBackendMap();
}
