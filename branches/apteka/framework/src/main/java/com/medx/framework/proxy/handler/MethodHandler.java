package com.medx.framework.proxy.handler;

public interface MethodHandler {
	public Object invoke(MapProxyAttributeProvider attributeProvider, Object[] args);
}
