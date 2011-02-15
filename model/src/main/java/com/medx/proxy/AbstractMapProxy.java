package com.medx.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.medx.attribute.AttrMap;
import com.medx.attribute.annotation.AttrKey;
import com.medx.type.TypeRegistry;
import com.medx.util.TextUtil;

abstract class AbstractMapProxy<T> implements InvocationHandler, MapProxy<T>, AttrMap {
	private static final Map<String, MethodHandler> handlers = new HashMap<String, MethodHandler>();

	protected static final Class<?>[] implementedInterfaces = {Map.class, MapProxy.class, AttrMap.class};
	
	protected final TypeRegistry typeRegistry;
	
	protected AbstractMapProxy(TypeRegistry typeRegistry) {
		this.typeRegistry = typeRegistry;
	}
	
	static {
		handlers.put(GetMethodHandler.getPrefix(), new GetMethodHandler());
		handlers.put(SetMethodHandler.getPrefix(), new SetMethodHandler());
	}
	
	protected abstract Object getAttributeValue(String attributeName);
	protected abstract void   setAttributeValue(String attributeName, Object value);
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getDeclaringClass() == Map.class)
			return method.invoke(getBackendMap(), args);
		
		if (method.getDeclaringClass() == AttrMap.class || method.getDeclaringClass() == MapProxy.class || method.getDeclaringClass() == Object.class)
			return method.invoke(this, args);
		
		String camelPrefix = TextUtil.getCamelPrefix(method.getName());
		
		String attributeName = method.getAnnotation(AttrKey.class).value();
		
		return handlers.get(camelPrefix).invoke(this, attributeName, args);
	}
	
	//protected List<?> wrapList(List<?> list, ProxyFactory<T> proxyFactory) {
	//	List<Object> result = new ArrayList<Object>(list.size());
	//}
}
