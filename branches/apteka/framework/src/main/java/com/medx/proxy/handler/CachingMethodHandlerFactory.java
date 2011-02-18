package com.medx.proxy.handler;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.medx.attribute.AttrKeyRegistry;
import com.medx.attribute.annotation.AttrKey;
import com.medx.util.TextUtil;

public class CachingMethodHandlerFactory implements MethodHandlerFactory {
	private final ConcurrentMap<Method, MethodHandler> handlerRegistry = new ConcurrentHashMap<Method, MethodHandler>();
	
	private final AttrKeyRegistry attrKeyRegistry;

	public CachingMethodHandlerFactory(AttrKeyRegistry attrKeyRegistry) {
		this.attrKeyRegistry = attrKeyRegistry;
	}

	public MethodHandler getMethodHandler(Method method) {
		if (handlerRegistry.containsKey(method))
			return handlerRegistry.get(method);
		
		String camelPrefix = TextUtil.getCamelPrefix(method.getName());
		String attributeName = method.getAnnotation(AttrKey.class).value();
		int attributeId = attrKeyRegistry.getAttrKey(attributeName).getId();
		
		handlerRegistry.putIfAbsent(method, MethodHandlerCreator.createMethodHandler(camelPrefix, attributeId));
		
		return handlerRegistry.get(method);
	}
}
