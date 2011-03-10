package com.medx.framework.proxy.serialization.xom.internal;

import java.util.HashSet;
import java.util.Set;

import nu.xom.Element;
import nu.xom.Elements;

import com.medx.framework.proxy.serialization.xom.InternalXomSerializer;
import com.medx.framework.proxy.serialization.xom.XomSerializationContext;

public class SetXomSerializer<T> implements InternalXomSerializer<Set<T>> {
	public static final String TAG = "set";
	
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

	@Override
	public Set<T> deserialize(Element set, XomSerializationContext context) {
		Set<T> result = new HashSet<T>();
		
		Elements elements = set.getChildElements();
		
		for (int i = 0; i < elements.size(); ++i) {
			InternalXomSerializer<T> elementSerializer = context.getXomSerializer(elements.get(i).getChildElements().get(0));
			result.add(elementSerializer.deserialize(elements.get(i).getChildElements().get(0), context));
		}
		
		return result;
	}
}
