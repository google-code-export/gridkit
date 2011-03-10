package com.medx.framework.proxy.serialization.xom.internal;

import java.lang.reflect.Array;

import nu.xom.Attribute;
import nu.xom.Element;

import com.medx.framework.proxy.serialization.xom.InternalXomSerializer;
import com.medx.framework.proxy.serialization.xom.XomSerializationContext;

public class ArrayXomSerializer implements InternalXomSerializer<Object> {
	@Override
	public Element serialize(Object array, XomSerializationContext context) {
		Element result = new Element("array");
		
		result.addAttribute(new Attribute("class", array.getClass().getComponentType().getCanonicalName()));
		
		for (int i = 0; i < Array.getLength(array); ++i) {
			Element elementElement = new Element("element");
			result.appendChild(elementElement);
			
			Object element = Array.get(array, i);
			
			InternalXomSerializer<Object> elementSerializer = context.getXomSerializer(element);
			elementElement.appendChild(elementSerializer.serialize(element, context));
		}
		
		return result;
	}
}
