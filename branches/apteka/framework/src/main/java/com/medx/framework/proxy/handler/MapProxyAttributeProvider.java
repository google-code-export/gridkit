package com.medx.framework.proxy.handler;

public interface MapProxyAttributeProvider {
	Object getAttributeValue(int attributeId);
	
	void setAttributeValue(int attributeId, Object value);
}
