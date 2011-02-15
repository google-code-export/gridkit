package com.medx.proxy;

interface MethodHandler {
	Object invoke(AbstractMapProxy<?> mapProxy, String attributeName, Object[] args);
}
