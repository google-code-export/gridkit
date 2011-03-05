package com.medx.framework.proxy.handler.standard;

import com.medx.framework.annotation.handler.AttributeAccessHandler;
import com.medx.framework.metadata.AttrKey;
import com.medx.framework.proxy.handler.MapProxyAttributeProvider;
import com.medx.framework.proxy.handler.MethodHandler;

@AttributeAccessHandler(verb = "get")
public class GetMethodHandler implements MethodHandler {
	private final int attributeId;
	
	public GetMethodHandler(AttrKey<?> key) {
		this.attributeId = key.getId();
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
