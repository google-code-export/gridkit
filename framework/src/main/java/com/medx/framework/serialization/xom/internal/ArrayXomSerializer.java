package com.medx.framework.serialization.xom.internal;

import java.lang.reflect.Array;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;

import com.medx.framework.serialization.xom.InternalXomSerializer;
import com.medx.framework.serialization.xom.XomSerializationContext;
import com.medx.framework.util.ClassUtil;

public class ArrayXomSerializer implements InternalXomSerializer<Object> {
	public static final String TAG = "array";
	
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

	@Override
	public Object deserialize(Element array, XomSerializationContext context) {
		Class<?> clazz = null;
		
		try {
			clazz = ClassUtil.getRawClassInstance(array.getAttributeValue("class"));
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("element");
		}
		
		Elements elements = array.getChildElements();
		
		Object result = Array.newInstance(clazz, elements.size());
		
		for (int i = 0; i < elements.size(); ++i) {
			InternalXomSerializer<Object> elementSerializer = context.getXomSerializer(elements.get(i).getChildElements().get(0));
			Array.set(result, i, elementSerializer.deserialize(elements.get(i).getChildElements().get(0), context));
		}
		
		return result;
	}
}
