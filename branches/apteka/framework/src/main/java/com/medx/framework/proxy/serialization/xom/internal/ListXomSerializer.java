package com.medx.framework.proxy.serialization.xom.internal;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;
import nu.xom.Elements;

import com.medx.framework.proxy.serialization.xom.InternalXomSerializer;
import com.medx.framework.proxy.serialization.xom.XomSerializationContext;

public class ListXomSerializer<T> implements InternalXomSerializer<List<T>> {
	public static final String TAG = "list";
	
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

	@Override
	public List<T> deserialize(Element list, XomSerializationContext context) {
		List<T> result = new ArrayList<T>();
		
		Elements elements = list.getChildElements();
		
		for (int i = 0; i < elements.size(); ++i) {
			InternalXomSerializer<T> elementSerializer = context.getXomSerializer(elements.get(i).getChildElements().get(0));
			result.add(elementSerializer.deserialize(elements.get(i).getChildElements().get(0), context));
		}
		
		return result;
	}
}
