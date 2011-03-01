package com.medx.framework.proxy.handler;

public interface MethodHandler {
	Object invoke(AttributeAccessor attributeAccessor, Object[] args);
}
