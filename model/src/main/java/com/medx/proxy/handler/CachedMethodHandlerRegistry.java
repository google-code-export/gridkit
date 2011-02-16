package com.medx.proxy.handler;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.medx.attribute.AttrKeyRegistry;
import com.medx.attribute.annotation.AttrKey;
import com.medx.util.TextUtil;

public class CachedMethodHandlerRegistry implements MethodHandlerRegistry {
	private final ConcurrentMap<Method, MethodHandler> handlerRegistry = new ConcurrentHashMap<Method, MethodHandler>();
	
	private final AttrKeyRegistry attrKeyRegistry;

	public CachedMethodHandlerRegistry(AttrKeyRegistry attrKeyRegistry) {
		this.attrKeyRegistry = attrKeyRegistry;
	}

	public MethodHandler getMethodHandler(Method method) {
		if (handlerRegistry.containsKey(method))
			return handlerRegistry.get(method);
		
		String camelPrefix = TextUtil.getCamelPrefix(method.getName());
		String attributeName = method.getAnnotation(AttrKey.class).value();
		int attributeId = attrKeyRegistry.getAttrKey(attributeName).getId();
		
		handlerRegistry.putIfAbsent(method, createMethodHandler(camelPrefix, attributeId));
		
		return handlerRegistry.get(method);
	}
	
	private static MethodHandler createMethodHandler(String camelPrefix, int attributeId) {
		if (GetMethodHandler.getPrefix().equals(camelPrefix))
			return new GetMethodHandler(attributeId);
		else if (SetMethodHandler.getPrefix().equals(camelPrefix))
			return new SetMethodHandler(attributeId);
		else
			throw new RuntimeException("Unknown camel prefix - " + camelPrefix);
	}
}
