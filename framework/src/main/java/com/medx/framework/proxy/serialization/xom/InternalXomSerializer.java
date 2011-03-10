package com.medx.framework.proxy.serialization.xom;

import nu.xom.Element;

public interface InternalXomSerializer<T> {
	Element serialize(T object, XomSerializationContext context);
	
	T deserialize(Element element, XomSerializationContext context);
}
