package com.medx.framework.proxy.serialization.xom.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import nu.xom.Element;

import com.medx.framework.proxy.serialization.xom.InternalXomSerializer;
import com.medx.framework.proxy.serialization.xom.XomSerializationContext;

public class PrimitiveXomSerializer implements InternalXomSerializer<Object> {
	public static final Collection<String> supportedTags;
	public static final Collection<Class<?>> supportedClasses;
	
	private static final Map<Class<?>, String> tagNameByClass = new HashMap<Class<?>, String>();
	
	static {
		tagNameByClass.put(Boolean.class, "boolean");
		tagNameByClass.put(Byte.class, "byte");
		tagNameByClass.put(Character.class, "char");
		tagNameByClass.put(Short.class, "short");
		tagNameByClass.put(Integer.class, "integer");
		tagNameByClass.put(Long.class, "long");
		tagNameByClass.put(Float.class, "float");
		tagNameByClass.put(Double.class, "double");
		tagNameByClass.put(String.class, "string");
		
		supportedTags = Collections.unmodifiableSet(new HashSet<String>(tagNameByClass.values()));
		supportedClasses = Collections.unmodifiableSet(new HashSet<Class<?>>(tagNameByClass.keySet()));
	}
	
	@Override
	public Element serialize(Object object, XomSerializationContext context) {
		Element result = new Element(tagNameByClass.get(object.getClass()));
		result.appendChild(object.toString());
		return result;
	}

	@Override
	public Object deserialize(Element element, XomSerializationContext context) {
		String tag = element.getLocalName().toLowerCase();
		
		if ("boolean".equals(tag))
			return Boolean.valueOf(element.getValue());
		else if ("byte".equals(tag))
			return Byte.valueOf(element.getValue());
		else if ("char".equals(tag) && element.getValue().length() == 1)
			return element.getValue().charAt(0);
		else if ("short".equals(tag))
			return Short.valueOf(element.getValue());
		else if ("integer".equals(tag))
			return Integer.valueOf(element.getValue());
		else if ("long".equals(tag))
			return Long.valueOf(element.getValue());
		else if ("float".equals(tag))
			return Float.valueOf(element.getValue());
		else if ("double".equals(tag))
			return Double.valueOf(element.getValue());
		else if ("string".equals(tag))
			return element.getValue();
		else
			throw new IllegalArgumentException("element");
	}
}
