package com.medx.proxy;

public class SetMethodHandler implements MethodHandler {
	public static String getPrefix() {
		return "set";
	}
	
	public static MethodHandlerFactory getFactory() {
		return new MethodHandlerFactory() {
			@Override
			public MethodHandler createMethodHandler(int attributeId) {
				return new SetMethodHandler(attributeId);
			}
		};
	}
	
	private final int attributeId;
	
	public SetMethodHandler(int attributeId) {
		this.attributeId = attributeId;
	}
	
	@Override
	public Object invoke(MapProxyImpl mapProxy, Object[] args) {
		if (args.length == 1)
			mapProxy.setAttributeValue(attributeId, args[0]);
		else
			throw new RuntimeException("Unexpected arguments count");
		
		return null;
	}
}
