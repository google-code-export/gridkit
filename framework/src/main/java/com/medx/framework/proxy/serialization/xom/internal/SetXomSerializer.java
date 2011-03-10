package com.medx.framework.proxy.serialization.xom.internal;

import java.util.Set;

import nu.xom.Element;

import com.medx.framework.proxy.serialization.xom.InternalXomSerializer;
import com.medx.framework.proxy.serialization.xom.XomSerializationContext;

public class SetXomSerializer<T> implements InternalXomSerializer<Set<T>> {
	@Override
	public Element serialize(Set<T> set, XomSerializationContext context) {
		Element result = new Element("set");
		
		for (T element : set) {
			Element elementElement = new Element("element");
			result.appendChild(elementElement);
			
			InternalXomSerializer<T> elementSerializer = context.getXomSerializer(element);
			elementElement.appendChild(elementSerializer.serialize(element, context));
		}
		
		return result;
	}
}
