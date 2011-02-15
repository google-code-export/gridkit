package com.medx.proxy;

import java.util.Map;

interface ProxyFactory<T> {
	Object createProxy(Map<T, Object> backendMap);
}
