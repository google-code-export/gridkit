package com.medx.proxy;

import com.medx.proxy.handler.MethodHandlerFactory;

interface InternalMapProxyFactory extends MapProxyFactory {
	MethodHandlerFactory getMethodHandlerFactory();
}
