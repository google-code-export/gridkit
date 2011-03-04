package com.medx.framework.proxy;

import com.medx.framework.metadata.TypeRegistry;
import com.medx.framework.proxy.handler.MethodHandlerFactory;

interface MapProxyFactoryInternal extends MapProxyFactory {
	TypeRegistry getTypeRegistry();
	
	MethodHandlerFactory getMethodHandlerFactory();
}
