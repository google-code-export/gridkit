package com.medx.framework.proxy.serialization.xom.internal;

import java.util.HashMap;
import java.util.Map;

import nu.xom.Element;

import com.medx.framework.proxy.serialization.xom.InternalXomSerializer;
import com.medx.framework.proxy.serialization.xom.XomSerializationContext;

public class PrimitiveXomSerializer implements InternalXomSerializer<Object> {
	public static final Map<Class<?>, String> tagNameByClass = new HashMap<Class<?>, String>();
	
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
	}
	
	@Override
	public Element serialize(Object object, XomSerializationContext context) {
		Element result = new Element(tagNameByClass.get(object.getClass()));
		
		result.appendChild(object.toString());
		
		return result;
	}
}
