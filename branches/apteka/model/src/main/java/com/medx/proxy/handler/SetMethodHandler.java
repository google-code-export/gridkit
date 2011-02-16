package com.medx.proxy.handler;

import com.medx.proxy.MapProxy;

public class SetMethodHandler implements MethodHandler {
	public static String getPrefix() {
		return "set";
	}
	
	private final int attributeId;
	
	public SetMethodHandler(int attributeId) {
		this.attributeId = attributeId;
	}
	
	@Override
	public Object invoke(MapProxy mapProxy, Object[] args) {
		if (args.length == 1)
			mapProxy.setAttributeValue(attributeId, args[0]);
		else
			throw new RuntimeException("Unexpected arguments count");
		
		return null;
	}
}
