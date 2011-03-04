package com.medx.framework.proxy.handler;

import com.medx.framework.annotation.handler.AttributeAccessHandler;
import com.medx.framework.attribute.AttrKey;

@AttributeAccessHandler(verb = "get", attributeType = Object.class)
public class GetMethodHandler implements MethodHandler {
	private int attributeId;

	// no arg constructor to use via reflection
	public GetMethodHandler() {		
	}
	
	public void setAttrKey(AttrKey<?> attrKey) {
		this.attributeId = attrKey.getId();
	}

	@Override
	public Object invoke(MapProxyAttributeProvider attributeProvider, Object[] args) {
		Object attribute = attributeProvider.getAttributeValue(attributeId);
		
		if (args == null || args.length == 0)
			return attribute;
		else
			throw new RuntimeException("Unexpected arguments count");
	}
}
