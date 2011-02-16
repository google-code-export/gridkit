package com.medx.proxy.handler;

public interface MethodHandler {
	Object invoke(AttributeAccessor attributeAccessor, Object[] args);
}
