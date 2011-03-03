package com.medx.framework.proxy;

import java.util.Map;

public interface MapProxyFactory {
	//TODO Integer.MIN_VALUE may cause serialization overhead
	public static final int CLASSES_KEY = Integer.MIN_VALUE;
	
	boolean isProxiable(Object object);
	
	<T> T createMapProxy(Map<Integer, Object> backendMap);
}
