package com.medx.framework.proxy.handler;

public interface MethodHandler {
	Object invoke(MapProxyAttributeProvider attributeProvider, Object[] args);
}
