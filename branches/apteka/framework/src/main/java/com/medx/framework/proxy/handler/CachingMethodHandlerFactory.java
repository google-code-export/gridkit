package com.medx.framework.proxy.handler;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.medx.framework.annotation.handler.AttributeAccessHandler;
import com.medx.framework.attribute.AttrKey;
import com.medx.framework.metadata.AttrKeyRegistry;
import com.medx.framework.util.TextUtil;

public class CachingMethodHandlerFactory implements MethodHandlerFactory {
	private final ConcurrentMap<Method, MethodHandler> handlerRegistry = new ConcurrentHashMap<Method, MethodHandler>();
	
	private final AttrKeyRegistry attrKeyRegistry;
	
	private final ConcurrentMap<String, MethodHandlerConstructor> methodMap = new ConcurrentHashMap<String, MethodHandlerConstructor>();

	public CachingMethodHandlerFactory(AttrKeyRegistry attrKeyRegistry) {
		this.attrKeyRegistry = attrKeyRegistry;
		registerHandler(GetMethodHandler.class);
		registerHandler(SetMethodHandler.class);
	}

	public void registerHandler(Class<?> handler) {
		AttributeAccessHandler annotation = handler.getAnnotation(AttributeAccessHandler.class);
		String prefix = annotation.verb();
		methodMap.put(prefix, new SimpleMethodHandlerFactory(handler));
	}
	
	public MethodHandler getMethodHandler(Method method) {
		if (handlerRegistry.containsKey(method))
			return handlerRegistry.get(method);
		
		AttrKey<?> attr = MethodHandlerUtil.getAttrKey(method, attrKeyRegistry);
		
		String verb = TextUtil.getCamelPrefix(method.getName());
		MethodHandler methodHandler = methodMap.get(verb).create(method, attr);
		
		handlerRegistry.putIfAbsent(method, methodHandler);
		
		return handlerRegistry.get(method);
	}
	
	private interface MethodHandlerConstructor {

		public abstract MethodHandler create(Method method, AttrKey<?> key);
	}
	
	private class SimpleMethodHandlerFactory implements MethodHandlerConstructor {
		
		private final Class<?> handlerClass;
		
		public SimpleMethodHandlerFactory(Class<?> handlerClass) {
			this.handlerClass = handlerClass;
		}
		
		public MethodHandler create(Method method, AttrKey<?> key) {
			// TODO use DI to initialize method handler
			// transaction management or other aspects could be added in future
			Object[] args = {key};
			MethodHandler handler;
			try {
				handler = (MethodHandler) handlerClass.newInstance();
			} catch (Exception e) {
				// TODO
				throw new RuntimeException(e);
			}
			handler.setAttrKey(key);
			return handler;
		}
	}	
}
