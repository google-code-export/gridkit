package com.medx.framework.serialization.xom.internal;

import nu.xom.Element;

import com.medx.framework.serialization.xom.InternalXomSerializer;
import com.medx.framework.serialization.xom.XomSerializationContext;

public class NullXomSerializer implements InternalXomSerializer<Object> {
	public static final String TAG = "null";
	
	@Override
	public Element serialize(Object object, XomSerializationContext context) {
		return new Element("null");
	}
	
	@Override
	public Object deserialize(Element element, XomSerializationContext context) {
		return null;
	}
}
