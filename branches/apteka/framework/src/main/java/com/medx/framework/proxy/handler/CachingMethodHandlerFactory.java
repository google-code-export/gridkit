package com.medx.framework.proxy.handler;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.medx.framework.annotation.handler.AttributeAccessHandler;
import com.medx.framework.attribute.AttrKey;
import com.medx.framework.metadata.AttrKeyRegistry;
import com.medx.framework.proxy.handler.standard.GetMethodHandler;
import com.medx.framework.proxy.handler.standard.SetMethodHandler;

public class CachingMethodHandlerFactory extends AbstractMethodHandlerFactory {
	private final ConcurrentMap<Method, MethodHandler> handlerRegistry = new ConcurrentHashMap<Method, MethodHandler>();
	private final ConcurrentMap<Class<?>, ClassNounInfo> classNounInfoRegistry = new ConcurrentHashMap<Class<?>, ClassNounInfo>();
	
	private final ConcurrentMap<String, MethodHandlerConstructor> methodMap = new ConcurrentHashMap<String, MethodHandlerConstructor>();

	public CachingMethodHandlerFactory(AttrKeyRegistry attrKeyRegistry) {
		super(attrKeyRegistry);
		
		registerHandler(GetMethodHandler.class);
		registerHandler(SetMethodHandler.class);
	}

	public void registerHandler(Class<?> handler) {
		AttributeAccessHandler annotation = handler.getAnnotation(AttributeAccessHandler.class);
		String prefix = annotation.verb();
		methodMap.put(prefix, new SimpleMethodHandlerFactory(handler));
	}
	
	public MethodHandler createMethodHandler(Method method) {
		if (handlerRegistry.containsKey(method))
			return handlerRegistry.get(method);
		
		MethodInfo methodInfo = getMethodInfo(method);
		
		MethodHandler methodHandler = methodMap.get(methodInfo.getVerb()).create(method, methodInfo.getAttrKey());
		
		handlerRegistry.putIfAbsent(method, methodHandler);
		
		return handlerRegistry.get(method);
	}
	
	protected ClassNounInfo getClassNounInfo(Class<?> clazz) {
		if (classNounInfoRegistry.containsKey(clazz))
			return classNounInfoRegistry.get(clazz);
		
		classNounInfoRegistry.putIfAbsent(clazz, super.getClassNounInfo(clazz));
		
		return classNounInfoRegistry.get(clazz);
	}
	
	private interface MethodHandlerConstructor {
		public abstract MethodHandler create(Method method, AttrKey<?> key);
	}
	
	private class SimpleMethodHandlerFactory implements MethodHandlerConstructor {
		private final Class<?> handlerClass;
		
		public SimpleMethodHandlerFactory(Class<?> handlerClass) {
			this.handlerClass = handlerClass;
		}
		
		// TODO use DI to initialize method handler
		// transaction management or other aspects could be added in future
		public MethodHandler create(Method method, AttrKey<?> key) {
			Object[] args = {key};
			
			MethodHandler handler;
			
			try {
				handler = (MethodHandler) handlerClass.getConstructor(AttrKey.class).newInstance(args);
			} catch (Exception e) {
				// TODO
				throw new RuntimeException(e);
			}
			
			return handler;
		}
	}	
}
