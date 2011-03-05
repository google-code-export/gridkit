package com.medx.framework.proxy.handler.standard;

import com.medx.framework.annotation.handler.AttributeAccessHandler;
import com.medx.framework.attribute.AttrKey;
import com.medx.framework.proxy.handler.MapProxyAttributeProvider;
import com.medx.framework.proxy.handler.MethodHandler;

@AttributeAccessHandler(verb = "set")
public class SetMethodHandler implements MethodHandler {
	private final int attributeId;
	
	public SetMethodHandler(AttrKey<?> key) {
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
