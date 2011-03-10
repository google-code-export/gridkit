package com.medx.framework.proxy.serialization.xom.internal;

import java.util.List;

import nu.xom.Element;

import com.medx.framework.proxy.serialization.xom.InternalXomSerializer;
import com.medx.framework.proxy.serialization.xom.XomSerializationContext;

public class ListXomSerializer<T> implements InternalXomSerializer<List<T>> {
	@Override
	public Element serialize(List<T> list, XomSerializationContext context) {
		Element result = new Element("list");
		
		for (T element : list) {
			Element elementElement = new Element("element");
			result.appendChild(elementElement);
			
			InternalXomSerializer<T> elementSerializer = context.getXomSerializer(element);
			elementElement.appendChild(elementSerializer.serialize(element, context));
		}
		
		return result;
	}
}
