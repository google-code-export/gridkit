package com.medx.processing.util;

import nu.xom.Attribute;
import nu.xom.Element;

import com.medx.processing.dictionary.DictionaryEntry;

public class XmlUtil {
	public static Element toXML(DictionaryEntry entry) {
		Element attribute = new Element("attribute");
		
		attribute.addAttribute(new Attribute("id", String.valueOf(entry.getId())));
		attribute.addAttribute(new Attribute("version", String.valueOf(entry.getVersion())));
		
		Element name = new Element("name");
		name.appendChild(entry.getName());
		attribute.appendChild(name);
		
		Element type = new Element("type");
		type.appendChild(entry.getType());
		attribute.appendChild(type);
		
		if (!entry.getDescription().isEmpty()) {
			Element description = new Element("description");
			description.appendChild(entry.getDescription());
			attribute.appendChild(description);
		}
		
		return attribute;
	}
}
