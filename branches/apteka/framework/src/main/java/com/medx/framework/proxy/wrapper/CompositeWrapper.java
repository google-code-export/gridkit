package com.medx.framework.proxy.wrapper;

public interface CompositeWrapper {
	boolean isWrappable(Object object);
	
	Object wrap(Object object, ObjectWrapper objectWrapper);
}
