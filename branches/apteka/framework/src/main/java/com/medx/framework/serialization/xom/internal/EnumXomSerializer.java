package com.medx.framework.serialization.xom.internal;

import static com.medx.framework.util.CastUtil.cast;

import nu.xom.Attribute;
import nu.xom.Element;

import com.medx.framework.serialization.xom.InternalXomSerializer;
import com.medx.framework.serialization.xom.XomSerializationContext;

public class EnumXomSerializer<T extends Enum<T>> implements InternalXomSerializer<Enum<T>> {
	public static final String TAG = "enum";
	
	@Override
	public Element serialize(Enum<T> object, XomSerializationContext context) {
		Element result = new Element("enum");
		
		result.addAttribute(new Attribute("class", object.getClass().getCanonicalName()));
		result.appendChild(object.toString());
		
		return result;
	}

	@Override
	public Enum<T> deserialize(Element element, XomSerializationContext context) {
		String className = element.getAttributeValue("class");
		
		Class<T> clazz = null;
		try {
			clazz = cast(Class.forName(className));
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("element");
		}
		
		return Enum.valueOf(clazz, element.getValue());
	}
}
