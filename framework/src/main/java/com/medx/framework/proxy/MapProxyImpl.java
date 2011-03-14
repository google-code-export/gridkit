package com.medx.framework.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.medx.framework.metadata.AttrMap;
import com.medx.framework.metadata.ClassKey;
import com.medx.framework.metadata.UserAttrKey;
import com.medx.framework.proxy.handler.MapProxyAttributeProvider;
import com.medx.framework.proxy.wrapper.CompositeWrapper;
import com.medx.framework.proxy.wrapper.ListWrapper;
import com.medx.framework.proxy.wrapper.MapWrapper;
import com.medx.framework.proxy.wrapper.ObjectWrapper;
import com.medx.framework.proxy.wrapper.SetWrapper;
import com.medx.framework.util.CastUtil;

public class MapProxyImpl implements InvocationHandler, MapProxy, AttrMap, MapProxyAttributeProvider, ObjectWrapper {
	private static List<CompositeWrapper> wrappers = new ArrayList<CompositeWrapper>();
	
	static {
		wrappers.add(new ListWrapper());
		wrappers.add(new MapWrapper());
		wrappers.add(new SetWrapper());
	}
	
	private final Map<Integer, Object> backendMap;
	private final Set<Integer> wrappedAttributeIds = new TreeSet<Integer>();
	
	private final MapProxyFactoryInternal mapProxyFactory;
	
	MapProxyImpl(Map<Integer, Object> backendMap, MapProxyFactoryInternal mapProxyFactory) {
		this.backendMap = backendMap;
		this.mapProxyFactory = mapProxyFactory;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getDeclaringClass() == Map.class)
			return method.invoke(getBackendMap(), args);
		
		if (method.getDeclaringClass() == AttrMap.class || method.getDeclaringClass() == MapProxy.class || method.getDeclaringClass() == Object.class)
			return method.invoke(this, args);

		return mapProxyFactory.getMethodHandlerFactory().createMethodHandler(method).invoke(this, args);
	}
	
	@Override
	public Object getAttributeValue(int attributeId) {
		if (wrappedAttributeIds.contains(attributeId))
			return backendMap.get(attributeId);
		
		Object wrappedAttribute = wrap(backendMap.get(attributeId));
		wrappedAttributeIds.add(attributeId);
		
		backendMap.put(attributeId, wrappedAttribute);
		return wrappedAttribute;
	}

	@Override
	public Object wrap(Object object) {
		if (object instanceof MapProxy)
			return object;
		
		if (mapProxyFactory.isProxiable(object))
			return mapProxyFactory.createMapProxy(CastUtil.<Map<Integer, Object>>cast(object));
		
		for (CompositeWrapper wrapper : wrappers)
			if (wrapper.isWrappable(object))
				return wrapper.wrap(object, this);
		
		return object;
	}
	
	@Override
	public void setAttributeValue(int attributeId, Object value) {
		if (value instanceof MapProxy)
			wrappedAttributeIds.add(attributeId);
		
		if (mapProxyFactory.isProxiable(value))
			wrappedAttributeIds.remove(attributeId);
		
		backendMap.put(attributeId, value);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <V> V getAttribute(UserAttrKey<V> key) {
		return (V)getAttributeValue(key.getId());
	}

	@Override
	public <V> void setAttribute(UserAttrKey<V> key, V value) {
		setAttributeValue(key.getId(), value);
	}
	
	@Override
	public <U> U cast(Class<U> clazz) {
		ClassKey typeKey = mapProxyFactory.getModelMetadata().getClassKey(clazz);
		
		if (typeKey == null)
			throw new IllegalArgumentException("clazz");
		
		backendMap.put(typeKey.getId(), Boolean.TRUE);
		
		return CastUtil.<U>cast(mapProxyFactory.createMapProxy(backendMap));
	}
	
	@Override
	public Map<Integer, Object> getBackendMap() {
		return backendMap;
	}

	@Override
	public int hashCode() {
		return backendMap.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return backendMap.equals(obj);
	}
	
	@Override
	public String toString() {
		return backendMap.toString();
	}
}
