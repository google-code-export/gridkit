package com.medx.proxy;

interface MethodHandler {
	Object invoke(MapProxyImpl mapProxy, Object[] args);
}
