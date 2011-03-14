package com.medx.framework.bean;

import java.util.Map;

public interface BeanManager {
	public static final Integer BEAN_KEY = Integer.MAX_VALUE;
	
	boolean isBeanMap(Object object);
	
	<T> T createBean(Map<Integer, Object> beanMap);
}
