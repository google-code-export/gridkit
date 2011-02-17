package com.medx.proxy.wrapper;

public interface CompositeWrapper {
	boolean isWrappable(Object object);
	
	Object wrap(Object object, ObjectWrapper objectWrapper);
}
