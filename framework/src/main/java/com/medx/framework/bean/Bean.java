package com.medx.framework.bean;

import java.util.Map;

import com.medx.framework.metadata.UserAttrKey;

public interface Bean {
	public <T> T cast(Class<T> type);

	public boolean isInstanceOf(Class<?> type);

	public void addType(Class<?> type);
	
	public void remoteType(Class<?> type);
	
	public <V> V getAttribute(UserAttrKey<V> key);

	public <V> void setAttribute(UserAttrKey<V> key, V value);
		
	public Map<Integer, Object> asMap();
	
	public BeanManager getBeanManager();
}
