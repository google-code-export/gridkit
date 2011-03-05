package com.medx.framework.proxy;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import com.medx.framework.attribute.AttrMap;
import com.medx.framework.metadata.ModelMetadata;
import com.medx.framework.proxy.handler.MethodHandlerFactory;

public class MapProxyFactoryImpl implements MapProxyFactoryInternal {
	private static final Class<?>[] implementedInterfaces = {Map.class, MapProxy.class, AttrMap.class};

	private final ModelMetadata modelMetadata;
	private final MethodHandlerFactory methodHandlerFactory;
	
	public MapProxyFactoryImpl(ModelMetadata modelMetadata, MethodHandlerFactory methodHandlerFactory) {
		this.modelMetadata = modelMetadata;
		this.methodHandlerFactory = methodHandlerFactory;
	}

	@Override
	public <T> T createMapProxy(Map<Integer, Object> backendMap) {
		Set<Integer> typeIds = modelMetadata.getTypeIds(backendMap.keySet());
		
		Object proxiableKey = backendMap.get(PROXIABLE_KEY);
		
		if (proxiableKey != null && !Boolean.class.isInstance(proxiableKey) && !(Boolean)proxiableKey)
			throw new IllegalArgumentException("backendMap");
		
		backendMap.put(PROXIABLE_KEY, Boolean.TRUE);
		
		Class<?>[] interfaces = Arrays.copyOf(implementedInterfaces, implementedInterfaces.length + typeIds.size());
		
		int i = 0;
		for(Integer typeId : typeIds)
			interfaces[implementedInterfaces.length + i++] = modelMetadata.getTypeKey(typeId).getClazz();
		
		MapProxyImpl mapProxyImpl = new MapProxyImpl(backendMap, this);
		
		@SuppressWarnings("unchecked")
		T result = (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, mapProxyImpl);
		
		return result;
	}

	@Override
	public boolean isProxiable(Object object) {
		if (object == null)
			return false;
		
		if (!Map.class.isInstance(object))
			return false;
		
		Object candidate = ((Map<?, ?>)object).get(PROXIABLE_KEY);
		
		if (candidate != null && candidate.getClass() == Boolean.class && (Boolean)candidate)
			return true;
		
		return false;
	}

	@Override
	public MethodHandlerFactory getMethodHandlerFactory() {
		return methodHandlerFactory;
	}

	@Override
	public ModelMetadata getModelMetadata() {
		return modelMetadata;
	}
}
