package org.apteka.insurance.dictionary.generator.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apteka.insurance.dictionary.generator.DictionaryEntry;

import static java.lang.String.format;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

public class XmlUtil {
	private static final Logger log = Logger.getLogger(XmlUtil.class.getCanonicalName());
	
	public static Element toXML(DictionaryEntry entry) {
		Element attribute = new Element("attribute");
		attribute.addAttribute(new Attribute("id", String.valueOf(entry.getId())));
		
		Element name = new Element("name");
		name.appendChild(entry.getName());
		attribute.appendChild(name);
		
		Element type = new Element("type");
		type.appendChild(entry.getType());
		attribute.appendChild(type);
		
		if (GeneralUtil.hasText(entry.getDescription())) {
			Element description = new Element("description");
			description.appendChild(entry.getDescription());
			attribute.appendChild(description);
		}
		
		return attribute;
	}
	
	private static int getMaximumId(Document dictionary) {
		int maxId = 0;
		
		Elements elements = dictionary.getRootElement().getChildElements();
		
		for (int i = 0; i < elements.size(); ++i) {
			int curId = Integer.valueOf(elements.get(i).getAttribute("id").getValue());
			if (curId > maxId)
				maxId = curId;
		}
		
		return maxId;
	}
	
	public static void populateDocument(Document dictionary, List<DictionaryEntry> entries) {
		List<DictionaryEntry> entriesToPopulate = new ArrayList<DictionaryEntry>();
		
		for (DictionaryEntry entry : entries) {
			if (dictionary.query(format("/attributes/attribute[name='%s']", entry.getName())).size() == 0)
				entriesToPopulate.add(entry);
			else
				log.warning(format("Failed to add dictionary entry with name '%s' because this name is already in use", entry.getName()));
		}
		
		int id = getMaximumId(dictionary);
		
		for (DictionaryEntry entry : entriesToPopulate) {
			entry.setId(++id);
			dictionary.getRootElement().appendChild(toXML(entry));
		}
	}
	
	public static Document createEmptyDictionary() {
		Element attribute = new Element("attributes");
		return new Document(attribute);
	}
}
