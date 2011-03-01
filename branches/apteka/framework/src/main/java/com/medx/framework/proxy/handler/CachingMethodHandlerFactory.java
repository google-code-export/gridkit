package com.medx.framework.proxy.handler;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.medx.framework.attribute.AttrKeyRegistry;
import com.medx.framework.util.TextUtil;

public class CachingMethodHandlerFactory implements MethodHandlerFactory {
	private final ConcurrentMap<Method, MethodHandler> handlerRegistry = new ConcurrentHashMap<Method, MethodHandler>();
	
	private final AttrKeyRegistry attrKeyRegistry;

	public CachingMethodHandlerFactory(AttrKeyRegistry attrKeyRegistry) {
		this.attrKeyRegistry = attrKeyRegistry;
	}

	public MethodHandler getMethodHandler(Method method) {
		if (handlerRegistry.containsKey(method))
			return handlerRegistry.get(method);
		
		int attributeId = MethodHandlerUtil.getAttrKey(method, attrKeyRegistry).getId();
		
		MethodHandler methodHandler = MethodHandlerUtil.createMethodHandler(TextUtil.getCamelPrefix(method.getName()), attributeId);
		
		handlerRegistry.putIfAbsent(method, methodHandler);
		
		return handlerRegistry.get(method);
	}
}
