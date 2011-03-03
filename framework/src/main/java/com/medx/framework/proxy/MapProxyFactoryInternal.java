package com.medx.framework.proxy;

import com.medx.framework.proxy.handler.MethodHandlerFactory;
import com.medx.framework.type.TypeRegistry;

interface MapProxyFactoryInternal extends MapProxyFactory {
	TypeRegistry getTypeRegistry();
	
	MethodHandlerFactory getMethodHandlerFactory();
}
