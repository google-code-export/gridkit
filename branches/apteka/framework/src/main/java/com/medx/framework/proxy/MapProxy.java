package com.medx.framework.proxy;

import java.util.Map;

public interface MapProxy {
	
	<T> T cast(Class<T> clazz);
	
	Map<Integer, Object> getBackendMap();
}
