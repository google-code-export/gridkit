package com.medx.proxy;

import java.util.Map;

public interface MapProxy {
	<U> U cast(Class<U> clazz);
	
	Map<Integer, Object> getBackendMap();
	
	Map<String, Object> exportBackendMap();
}
