package com.medx.framework.proxy.serialization.xom;

import nu.xom.Element;

public interface XomSerializer<T> {
	Element serialize(T object, XomSerializationContext context);
}
