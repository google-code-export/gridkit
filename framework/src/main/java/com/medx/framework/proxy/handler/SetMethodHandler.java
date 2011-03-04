package com.medx.framework.proxy.handler;

import com.medx.framework.annotation.handler.AttributeAccessHandler;
import com.medx.framework.attribute.AttrKey;

@AttributeAccessHandler(verb = "set", attributeType = Object.class)
public class SetMethodHandler implements MethodHandler {
	private int attributeId;
	
	// no arg constructor to use via reflection
	public SetMethodHandler() {
	}

	@Override
	public void setAttrKey(AttrKey<?> key) {
		this.attributeId = key.getId();
	}
	
	@Override
	public Object invoke(MapProxyAttributeProvider attributeProvider, Object[] args) {
		if (args != null && args.length == 1)
			attributeProvider.setAttributeValue(attributeId, args[0]);
		else
			throw new RuntimeException("Unexpected arguments count");
		
		return null;
	}
}
