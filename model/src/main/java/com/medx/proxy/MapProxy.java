package com.medx.proxy;

import java.util.Map;

public interface MapProxy {
	<T> T cast(Class<T> clazz);
	
	boolean isCapableToImplement(Class<?> clazz);
	
	Map<Integer, Object> getBackendMap();
	
	Map<String, Object> exportBackendMap();
}
