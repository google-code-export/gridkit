package com.medx.framework.proxy.handler;

public class SetMethodHandler implements MethodHandler {
	public static final String PREFIX = "set";
	
	private final int attributeId;
	
	public SetMethodHandler(int attributeId) {
		this.attributeId = attributeId;
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
