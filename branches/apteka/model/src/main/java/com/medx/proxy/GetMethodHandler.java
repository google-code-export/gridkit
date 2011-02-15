package com.medx.proxy;

import java.util.Map;

class GetMethodHandler implements MethodHandler {
	public static String getPrefix() {
		return "get";
	}
	
	@Override
	public Object invoke(AbstractMapProxy<?> mapProxy, String attributeName, Object[] args) {
		Object attribute = mapProxy.getAttributeValue(attributeName);
		
		if (args.length == 0)
			return attribute;
		else if (args.length == 1)
			return ((Map<?, ?>)attribute).get(args[0]);
		else
			throw new RuntimeException("Unexpected arguments count");
	}
}
