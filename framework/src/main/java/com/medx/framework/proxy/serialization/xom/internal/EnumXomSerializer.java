package com.medx.framework.proxy.serialization.xom.internal;

import nu.xom.Attribute;
import nu.xom.Element;

import com.medx.framework.proxy.serialization.xom.InternalXomSerializer;
import com.medx.framework.proxy.serialization.xom.XomSerializationContext;

public class EnumXomSerializer implements InternalXomSerializer<Enum<?>> {
	@Override
	public Element serialize(Enum<?> object, XomSerializationContext context) {
		Element result = new Element("enum");
		
		result.addAttribute(new Attribute("class", object.getClass().getCanonicalName()));
		result.appendChild(object.toString());
		
		return result;
	}
}
