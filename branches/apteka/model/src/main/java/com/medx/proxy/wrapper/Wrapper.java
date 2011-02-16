package com.medx.proxy.wrapper;

public interface Wrapper {
	boolean isWrappable(Object object);
	
	Object wrap(Object object, Wrapper objectWrapper);
}
