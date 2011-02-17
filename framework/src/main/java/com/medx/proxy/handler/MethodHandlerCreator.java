package com.medx.proxy.handler;

public class MethodHandlerCreator {
	public static MethodHandler createMethodHandler(String camelPrefix, int attributeId) {
		if (GetMethodHandler.getPrefix().equals(camelPrefix))
			return new GetMethodHandler(attributeId);
		else if (SetMethodHandler.getPrefix().equals(camelPrefix))
			return new SetMethodHandler(attributeId);
		else
			throw new RuntimeException("Unknown camel prefix - " + camelPrefix);
	}
}
