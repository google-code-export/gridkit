package com.medx.framework.proxy.handler;

import com.medx.framework.attribute.AttrKey;

public interface MethodHandler {
	
	public void setAttrKey(AttrKey<?> key);
	public Object invoke(MapProxyAttributeProvider attributeProvider, Object[] args);
}
