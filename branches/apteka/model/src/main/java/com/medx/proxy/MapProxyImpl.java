package com.medx.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.medx.attribute.AttrKey;
import com.medx.attribute.AttrMap;
import com.medx.proxy.wrapper.ListWrapper;
import com.medx.proxy.wrapper.Wrapper;
import com.medx.util.CastUtil;

public class MapProxyImpl implements InvocationHandler, MapProxy, AttrMap {
	private static List<Wrapper> wrappers = new ArrayList<Wrapper>();
	
	static {
		wrappers.add(new ListWrapper());
	}
	
	private final Map<Integer, Object> backendMap;
	private Map<Integer, Object> wrappedBackendMap;
	
	MapProxyImpl(Map<Integer, Object> backendMap) {
		this.backendMap = backendMap;
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

		return MapProxyFactory.Instance.methodHandlerRegistry.getMethodHandler(method).invoke(this, args);
	}
	
	@Override
	public Object getAttributeValue(int attributeId) {
		if (getWrappedBackendMap().containsKey(attributeId))
			return getWrappedBackendMap().get(attributeId);
		
		MapProxyFactory proxyFactory = MapProxyFactory.Instance.getInstance();
		
		Object result = backendMap.get(attributeId);
		
		if (result == null)
			return result;
		
		boolean cacheResult = false;
		
		if (proxyFactory.isWrappable(result)) {
			result = proxyFactory.createProxy(CastUtil.<Map<Integer, Object>>cast(result));
			cacheResult = true;
		}
		else
			for (Wrapper wrapper : wrappers)
				if (wrapper.isApplicable(result)) {
					result = wrapper.wrap(result);
					cacheResult = true;
					break;
				}
		
		if (cacheResult)
			getWrappedBackendMap().put(attributeId, result);
		
		return result;
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
