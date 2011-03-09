package com.medx.framework.proxy.serialization.xml;

import nu.xom.Element;

public interface XomSerializer<T> {
	Element serialize(T object, XomSerializationContext context);
}
