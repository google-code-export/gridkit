package com.medx.proxy.handler;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.medx.attribute.AttrKeyRegistry;
import com.medx.util.TextUtil;

public class CachingMethodHandlerFactory implements MethodHandlerFactory {
	private final ConcurrentMap<Method, MethodHandler> handlerRegistry = new ConcurrentHashMap<Method, MethodHandler>();
	
	private final AttrKeyRegistry attrKeyRegistry;
	
	private final String cutPrefix;
	private final String addPrefix;

	public CachingMethodHandlerFactory(AttrKeyRegistry attrKeyRegistry, String cutPrefix, String addPrefix) {
		this.attrKeyRegistry = attrKeyRegistry;
		this.cutPrefix = cutPrefix;
		this.addPrefix = addPrefix;
	}
	
	public CachingMethodHandlerFactory(AttrKeyRegistry attrKeyRegistry, String cutPrefix) {
		this(attrKeyRegistry, cutPrefix, "");
	}

	public MethodHandler getMethodHandler(Method method) {
		if (handlerRegistry.containsKey(method))
			return handlerRegistry.get(method);
		
		String camelPrefix = TextUtil.getCamelPrefix(method.getName());

		int attributeId = attrKeyRegistry.getAttrKey(getAttributeName(method)).getId();
		
		handlerRegistry.putIfAbsent(method, MethodHandlerCreator.createMethodHandler(camelPrefix, attributeId));
		
		return handlerRegistry.get(method);
	}
	
	private String getAttributeName(Method method) {
		return (addPrefix.isEmpty() ? "" : addPrefix + ".") + 
			method.getDeclaringClass().getCanonicalName().substring(cutPrefix.length() + 1) + "." + 
			getGetterName(method.getName());
	}
	
	private static String getGetterName(String methodName) {
		int index = 0;
		for (; index < methodName.length(); ++index)
			if (Character.isUpperCase(methodName.charAt(index)))
				break;
		
		return Character.toLowerCase(methodName.charAt(index)) + methodName.substring(index + 1);
	}
}
