package com.medx.framework.proxy.serialization.xom.internal;

import java.util.Map;

import nu.xom.Element;

import com.medx.framework.proxy.serialization.xom.InternalXomSerializer;
import com.medx.framework.proxy.serialization.xom.XomSerializationContext;

public class MapXomSerializer<K, V> implements InternalXomSerializer<Map<K, V>> {
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
}
