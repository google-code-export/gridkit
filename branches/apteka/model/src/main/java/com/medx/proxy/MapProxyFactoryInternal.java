package com.medx.proxy;

import com.medx.proxy.handler.MethodHandlerFactory;

interface MapProxyFactoryInternal extends MapProxyFactory {
	MethodHandlerFactory getMethodHandlerFactory();
}
