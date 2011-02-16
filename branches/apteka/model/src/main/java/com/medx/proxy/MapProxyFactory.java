package com.medx.proxy;

import java.util.Map;

public interface MapProxyFactory {
	boolean isProxiable(Object object);
	
	<T> T createMapProxy(Map<Integer, Object> backendMap);
}
