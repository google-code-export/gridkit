package com.medx.proxy;

import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.medx.attribute.AttrKey;
import com.medx.attribute.AttrMap;
import com.medx.type.TypeRegistry;

public final class StringMapProxy extends AbstractMapProxy<String> {
	public static final String CLASSES_KEY = "com.medx.proxy.StringMapProxy.CLASSES_KEY";
	
	private final Map<String, Object> backendMap;
	
	private final Map<String, Object> wrappedBackendMap = new HashMap<String, Object>();
	
	private static final ProxyFactory<String> proxyFactory = new ProxyFactory<String>() {
		@Override
		public Object createProxy(Map<String, Object> backendMap) {
			//interfaces = Arrays.copyOf(interfaces, interfaces.length + 2);
			
			//interfaces[interfaces.length - 2] = Map.class;
			//interfaces[interfaces.length - 1] = AttrMap.class;
			
			//new StringMapProxy(backendMap)
			return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), null, null);
		}
	};
	
	@SuppressWarnings("unchecked")
	public static <T> T createProxy(Map<String, Object> backendMap) {
		return (T) proxyFactory.createProxy(backendMap);
	}
	
	private StringMapProxy(Map<String, Object> backendMap, TypeRegistry typeRegistry) {
		super(typeRegistry);
		this.backendMap = backendMap;
	}

	@Override
	protected Object getAttributeValue(String attributeName) {
		return null;
	}

	@Override
	protected void setAttributeValue(String attributeName, Object value) {
		
	}
	
	@Override
	public <V> V get(AttrKey<V> key) {
		return null;
	}

	@Override
	public <V> V set(AttrKey<V> key, V value) {
		return null;
	}
	
	@Override
	public <U> U cast(Class<U> clazz) {
		return null;
	}
	
	@Override
	public Map<String, Object> getBackendMap() {
		return backendMap;
	}
}
