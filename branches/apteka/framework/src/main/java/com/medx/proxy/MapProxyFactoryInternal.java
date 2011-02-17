package com.medx.proxy;

import com.medx.proxy.handler.MethodHandlerFactory;
import com.medx.type.TypeRegistry;

interface MapProxyFactoryInternal extends MapProxyFactory {
	MethodHandlerFactory getMethodHandlerFactory();
	TypeRegistry getTypeRegistry();
}
