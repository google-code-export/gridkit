package com.medx.framework.proxy.handler;

import java.util.Collection;

import com.medx.framework.annotation.handler.AttributeAccessHandler;
import com.medx.framework.annotation.handler.NoonForm;
import com.medx.framework.attribute.AttrKey;

@AttributeAccessHandler(verb = "add", attributeType = Collection.class, noonForm = NoonForm.SINGULAR)
public class AddMethodHandler implements MethodHandler {

	public static final String PREFIX = "add";

	private AttrKey attrKey;
	
	@Override
	public void setAttrKey(AttrKey<?> key) {
		this.attrKey = key;
	}

	// no arg constructor to be accessed via reflection
	public AddMethodHandler() {
	}
	
	@Override
	public Object invoke(MapProxyAttributeProvider attributeProvider, Object[] args) {
		// TODO
//		if (args != null && args.length == 1)
//			attributeProvider.setAttributeValue(attributeId, args[0]);
//		else
//			throw new RuntimeException("Unexpected arguments count");
//		
//		return null;
		return null;
	}
}
