package com.medx.framework.proxy;

import java.util.Map;

public interface MapProxyFactory {
	public static final Integer PROXIABLE_KEY = Integer.MAX_VALUE;
	
	boolean isProxiable(Object object);
	
	<T> T createMapProxy(Map<Integer, Object> backendMap);
}
