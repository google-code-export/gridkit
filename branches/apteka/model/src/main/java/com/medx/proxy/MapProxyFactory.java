package com.medx.proxy;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;

import com.medx.attribute.AttrKeyRegistry;
import com.medx.attribute.AttrMap;
import com.medx.proxy.handler.CachedMethodHandlerRegistry;
import com.medx.proxy.handler.MethodHandlerRegistry;
import com.medx.type.TypeRegistry;

public interface MapProxyFactory {
	public static final int CLASSES_KEY = Integer.MIN_VALUE;
	
	<T> T createProxy(Map<Integer, Object> backendMap);
	
	boolean isWrappable(Object object);
	
	public static class Initialization {
		static volatile TypeRegistry typeRegistry;
		static volatile AttrKeyRegistry attrKeyRegistry;
		static volatile MethodHandlerRegistry methodHandlerRegistry;
		
		public static void init(TypeRegistry typeRegistry, AttrKeyRegistry attrKeyRegistry) {
			Initialization.typeRegistry = typeRegistry;
			Initialization.attrKeyRegistry = attrKeyRegistry;
			Initialization.methodHandlerRegistry = new CachedMethodHandlerRegistry(attrKeyRegistry);
		}
	}
	
	public static class Instance {
		public static final Class<?>[] implementedInterfaces = {Map.class, MapProxy.class, AttrMap.class};
		
		static final TypeRegistry typeRegistry = Initialization.typeRegistry;
		static final AttrKeyRegistry attrKeyRegistry = Initialization.attrKeyRegistry;
		static final MethodHandlerRegistry methodHandlerRegistry = Initialization.methodHandlerRegistry;
		
		private static final MapProxyFactory instance = new MapProxyFactory() {
			public <T> T createProxy(Map<Integer, Object> backendMap) {
				int[] interfaceIds = (int[])backendMap.get(CLASSES_KEY);
				
				Class<?>[] interfaces = Arrays.copyOf(implementedInterfaces, implementedInterfaces.length + interfaceIds.length);
				
				for (int i = implementedInterfaces.length; i < interfaces.length; ++i)
					interfaces[i] = typeRegistry.getType(interfaceIds[i]);
				
				MapProxyImpl mapProxyImpl = new MapProxyImpl(backendMap);
				
				@SuppressWarnings("unchecked")
				T result = (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, mapProxyImpl);
				
				return result;
			}

			public boolean isWrappable(Object object) {
				if (object == null || object.getClass() != Map.class)
					return false;
				
				Object candidate = ((Map<?, ?>)object).get(CLASSES_KEY);
				
				if (candidate != null && candidate.getClass() == int[].class)
					return true;
				
				return false;
			}
		};
		
		public static MapProxyFactory getInstance() {
			return instance;
		}
	}
}
