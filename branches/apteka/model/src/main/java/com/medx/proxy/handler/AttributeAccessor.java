package com.medx.proxy.handler;

public interface AttributeAccessor {
	Object getAttributeValue(int attributeId);
	
	void setAttributeValue(int attributeId, Object value);
}
