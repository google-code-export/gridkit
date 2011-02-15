package com.medx.proxy;

import java.util.Map;

class GetMethodHandler implements MethodHandler {
	public static String getPrefix() {
		return "get";
	}
	
	public static MethodHandlerFactory getFactory() {
		return new MethodHandlerFactory() {
			@Override
			public MethodHandler createMethodHandler(int attributeId) {
				return new GetMethodHandler(attributeId);
			}
		};
	}
	
	private final int attributeId;
	
	public GetMethodHandler(int attributeId) {
		this.attributeId = attributeId;
	}

	@Override
	public Object invoke(MapProxyImpl mapProxy, Object[] args) {
		Object attribute = mapProxy.getAttributeValue(attributeId);
		
		if (args.length == 0)
			return attribute;
		else if (args.length == 1)
			return ((Map<?, ?>)attribute).get(args[0]);
		else
			throw new RuntimeException("Unexpected arguments count");
	}
}
