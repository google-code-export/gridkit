package com.medx.proxy;

import java.util.Map;

public interface MapProxy<T> {
	<U> U cast(Class<U> clazz);
	
	Map<T, Object> getBackendMap();
}
