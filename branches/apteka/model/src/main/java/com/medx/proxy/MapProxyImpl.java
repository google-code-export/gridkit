package com.medx.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.medx.attribute.AttrKeyRegistry;
import com.medx.attribute.AttrMap;
import com.medx.attribute.annotation.AttrKey;
import com.medx.type.TypeRegistry;
import com.medx.util.TextUtil;

public abstract class MapProxyImpl implements InvocationHandler, MapProxy, AttrMap {
	public static final int CLASSES_KEY = Integer.MIN_VALUE;
	
	private static final Map<String, MethodHandlerFactory> handlerFactory = new HashMap<String, MethodHandlerFactory>();
	
	// TODO it is possible to implement static initialization of this field
	private static final ConcurrentMap<Method, MethodHandler> handlerRegistry = new ConcurrentHashMap<Method, MethodHandler>();
	
	static {
		handlerFactory.put(GetMethodHandler.getPrefix(), GetMethodHandler.getFactory());
		handlerFactory.put(SetMethodHandler.getPrefix(), SetMethodHandler.getFactory());
	}
	
	protected static final Class<?>[] implementedInterfaces = {Map.class, MapProxy.class, AttrMap.class};
	
	protected final TypeRegistry typeRegistry;
	protected final AttrKeyRegistry attrKeyRegistry;
	
	private final Map<Integer, Object> backendMap;
	private final Map<Integer, Object> wrappedBackendMap = new HashMap<Integer, Object>();
	
	protected MapProxyImpl(Map<Integer, Object> backendMap, TypeRegistry typeRegistry, AttrKeyRegistry attrKeyRegistry) {
		this.backendMap = backendMap;
		this.typeRegistry = typeRegistry;
		this.attrKeyRegistry = attrKeyRegistry;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getDeclaringClass() == Map.class)
			return method.invoke(getBackendMap(), args);
		
		if (method.getDeclaringClass() == AttrMap.class || method.getDeclaringClass() == MapProxy.class || method.getDeclaringClass() == Object.class)
			return method.invoke(this, args);

		return getMethodHandler(method).invoke(this, args);
	}
	
	Object getAttributeValue(int id) {
		if (wrappedBackendMap.containsKey(id))
			return wrappedBackendMap.get(id);
		
		return null;
	}
	
	void setAttributeValue(int id, Object value) {
		
	}
	
	private MethodHandler getMethodHandler(Method method) {
		if (handlerRegistry.containsKey(method))
			return handlerRegistry.get(method);
		
		String camelPrefix = TextUtil.getCamelPrefix(method.getName());
		String attributeName = method.getAnnotation(AttrKey.class).value();
		int attributeId = attrKeyRegistry.getAttrKey(attributeName).getId();
		
		handlerRegistry.putIfAbsent(method, handlerFactory.get(camelPrefix).createMethodHandler(attributeId));
		
		return handlerRegistry.get(method);
	}
	
	//protected List<?> wrapList(List<?> list, ProxyFactory<T> proxyFactory) {
	//	List<Object> result = new ArrayList<Object>(list.size());
	//}
	
	@Override
	@SuppressWarnings("unchecked")
	public <V> V get(com.medx.attribute.AttrKey<V> key) {
		return (V)getAttributeValue(key.getId());
	}

	@Override
	public <V> void set(com.medx.attribute.AttrKey<V> key, V value) {
		setAttributeValue(key.getId(), value);
	}
	
	@Override
	public <U> U cast(Class<U> clazz) {
		return null;
	}
	
	@Override
	public Map<Integer, Object> getBackendMap() {
		return backendMap;
	}
}
