package com.medx.framework.proxy.handler;

public class GetMethodHandler implements MethodHandler {
	public static final String PREFIX = "get";
	
	private final int attributeId;
	
	public GetMethodHandler(int attributeId) {
		this.attributeId = attributeId;
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
