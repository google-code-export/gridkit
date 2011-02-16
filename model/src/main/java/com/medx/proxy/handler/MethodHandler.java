package com.medx.proxy.handler;

import com.medx.proxy.MapProxy;

public interface MethodHandler {
	Object invoke(MapProxy mapProxy, Object[] args);
}
