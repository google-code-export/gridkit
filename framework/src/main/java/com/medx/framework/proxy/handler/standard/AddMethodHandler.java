package com.medx.framework.proxy.handler.standard;

import java.util.Collection;

import com.medx.framework.annotation.handler.AttributeAccessHandler;
import com.medx.framework.annotation.handler.NounForm;
import com.medx.framework.attribute.AttrKey;
import com.medx.framework.proxy.handler.MapProxyAttributeProvider;
import com.medx.framework.proxy.handler.MethodHandler;

@AttributeAccessHandler(verb = "add", attributeType = Collection.class, nounForm = NounForm.SINGULAR)
public class AddMethodHandler implements MethodHandler {
	private final int attributeId;
	
	public AddMethodHandler(AttrKey<?> key) {
		this.attributeId = key.getId();
	}
	
	@Override
	public Object invoke(MapProxyAttributeProvider attributeProvider, Object[] args) {
		int i = attributeId; ++i;
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
