package com.medx.framework.serialization.xom.impl;

import java.util.HashMap;
import java.util.Map;

import nu.xom.Element;
import nu.xom.Elements;

import com.medx.framework.serialization.xom.InternalXomSerializer;
import com.medx.framework.serialization.xom.XomSerializationContext;

public class MapXomSerializer<K, V> implements InternalXomSerializer<Map<K, V>> {
	public static final String TAG = "map";
	
	@Override
	public Element serialize(Map<K, V> map, XomSerializationContext context) {
		Element result = new Element("map");
		
		for (Map.Entry<K, V> entry : map.entrySet()) {
			Element entryElement = new Element("entry");
			result.appendChild(entryElement);
			
			Element keyElement = new Element("key");
			Element valueElement = new Element("value");
			
			entryElement.appendChild(keyElement);
			entryElement.appendChild(valueElement);
			
			InternalXomSerializer<K> keySerializer = context.getXomSerializer(entry.getKey());
			InternalXomSerializer<V> valueSerializer = context.getXomSerializer(entry.getValue());
			
			keyElement.appendChild(keySerializer.serialize(entry.getKey(), context));
			valueElement.appendChild(valueSerializer.serialize(entry.getValue(), context));
		}
		
		return result;
	}

	@Override
	public Map<K, V> deserialize(Element map, XomSerializationContext context) {
		Map<K, V> result = new HashMap<K, V>();
		
		Elements elements = map.getChildElements();
		
		for (int i = 0; i < elements.size(); ++i) {
			Element key = elements.get(i).getChildElements("key").get(0).getChildElements().get(0);
			Element value = elements.get(i).getChildElements("value").get(0).getChildElements().get(0);
			
			InternalXomSerializer<K> keySerializer = context.getXomSerializer(key);
			InternalXomSerializer<V> valueSerializer = context.getXomSerializer(value);
			
			result.put(keySerializer.deserialize(key, context), valueSerializer.deserialize(value, context));
		}
		
		return result;
	}
}
