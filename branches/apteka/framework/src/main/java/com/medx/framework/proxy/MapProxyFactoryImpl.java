package com.medx.framework.proxy;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;

import com.medx.framework.attribute.AttrMap;
import com.medx.framework.proxy.handler.MethodHandlerFactory;
import com.medx.framework.type.TypeRegistry;

public class MapProxyFactoryImpl implements MapProxyFactoryInternal {
	private static final Class<?>[] implementedInterfaces = {Map.class, MapProxy.class, AttrMap.class};
	
	private final TypeRegistry typeRegistry;
	private final MethodHandlerFactory methodHandlerFactory;
	
	public MapProxyFactoryImpl(TypeRegistry typeRegistry, MethodHandlerFactory methodHandlerFactory) {
		this.typeRegistry = typeRegistry;
		this.methodHandlerFactory = methodHandlerFactory;
	}

	@Override
	public <T> T createMapProxy(Map<Integer, Object> backendMap) {
		int[] interfaceIds = (int[])backendMap.get(CLASSES_KEY);
		
		Class<?>[] interfaces = Arrays.copyOf(implementedInterfaces, implementedInterfaces.length + interfaceIds.length);
		
		for (int i = implementedInterfaces.length; i < interfaces.length; ++i)
			interfaces[i] = typeRegistry.getType(interfaceIds[i - implementedInterfaces.length]);
		
		MapProxyImpl mapProxyImpl = new MapProxyImpl(backendMap, this);
		
		@SuppressWarnings("unchecked")
		T result = (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, mapProxyImpl);
		
		return result;
	}

	@Override
	public boolean isProxiable(Object object) {
		if (object == null || !Map.class.isInstance(object))
			return false;
		
		Object candidate = ((Map<?, ?>)object).get(CLASSES_KEY);
		
		if (candidate != null && candidate.getClass() == int[].class)
			return true;
		
		return false;
	}

	@Override
	public MethodHandlerFactory getMethodHandlerFactory() {
		return methodHandlerFactory;
	}

	@Override
	public TypeRegistry getTypeRegistry() {
		return typeRegistry;
	}
}
