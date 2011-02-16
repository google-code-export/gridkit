package com.medx.proxy.wrapper;

public interface Wrapper<T> {
	boolean isWrappable(Object object);
	
	T wrap(Object object, Wrapper<?> objectWrapper);
}
