package com.medx.proxy.wrapper;

public interface Wrapper {
	boolean isApplicable(Object object);
	
	Object wrap(Object result);
}
