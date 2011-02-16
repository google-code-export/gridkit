package com.medx.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.medx.attribute.AttrKey;
import com.medx.attribute.AttrMap;
import com.medx.proxy.handler.AttributeAccessor;
import com.medx.proxy.wrapper.ListWrapper;
import com.medx.proxy.wrapper.Wrapper;
import com.medx.util.CastUtil;

public class MapProxyImpl implements InvocationHandler, MapProxy, AttrMap, AttributeAccessor, Wrapper {
	private static List<Wrapper> wrappers = new ArrayList<Wrapper>();
	
	static {
		wrappers.add(new ListWrapper());
	}
	
	private final Map<Integer, Object> backendMap;
	private Map<Integer, Object> wrappedBackendMap;
	
	private final InternalMapProxyFactory mapProxyFactory;
	
	MapProxyImpl(Map<Integer, Object> backendMap, InternalMapProxyFactory mapProxyFactory) {
		this.backendMap = backendMap;
		this.mapProxyFactory = mapProxyFactory;
	}

	private Map<Integer, Object> getWrappedBackendMap() {
		if (wrappedBackendMap == null)
			wrappedBackendMap = new HashMap<Integer, Object>();
		
		return wrappedBackendMap;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getDeclaringClass() == Map.class)
			return method.invoke(getBackendMap(), args);
		
		if (method.getDeclaringClass() == AttrMap.class || method.getDeclaringClass() == MapProxy.class || method.getDeclaringClass() == Object.class)
			return method.invoke(this, args);

		return mapProxyFactory.getMethodHandlerFactory().getMethodHandler(method).invoke(this, args);
	}
	
	@Override
	public Object getAttributeValue(int attributeId) {
		if (getWrappedBackendMap().containsKey(attributeId))
			return getWrappedBackendMap().get(attributeId);
		
		Object attribute = backendMap.get(attributeId);
		
		if (!isWrappable(attribute))
			return attribute;
		
		Object wrappedAttribute = wrap(attribute, this);
		
		getWrappedBackendMap().put(attributeId, wrappedAttribute);
		
		return wrappedAttribute;
	}
	
	@Override
	public boolean isWrappable(Object object) {
		if (mapProxyFactory.isProxiable(object))
			return true;
		
		for (Wrapper wrapper : wrappers)
			if (wrapper.isWrappable(object))
				return true;
		
		return false;
	}

	@Override
	public Object wrap(Object object, Wrapper objectWrapper) {
		if (mapProxyFactory.isProxiable(object))
			return mapProxyFactory.createMapProxy(CastUtil.<Map<Integer, Object>>cast(object));
		
		for (Wrapper wrapper : wrappers)
			if (wrapper.isWrappable(object))
				return wrapper.wrap(object, objectWrapper);
		
		return null;
	}
	
	@Override
	public void setAttributeValue(int attributeId, Object value) {
		
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <V> V get(AttrKey<V> key) {
		return (V)getAttributeValue(key.getId());
	}

	@Override
	public <V> void set(AttrKey<V> key, V value) {
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

	@Override
	public Map<String, Object> exportBackendMap() {
		return null;
	}
}
