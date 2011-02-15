package com.medx.proxy;

public class SetMethodHandler implements MethodHandler {
	public static String getPrefix() {
		return "set";
	}
	
	@Override
	public Object invoke(AbstractMapProxy<?> mapProxy, String attributeName, Object[] args) {
		if (args.length == 1)
			mapProxy.setAttributeValue(attributeName, args[0]);
		else
			throw new RuntimeException("Unexpected arguments count");
		
		return null;
	}
}
