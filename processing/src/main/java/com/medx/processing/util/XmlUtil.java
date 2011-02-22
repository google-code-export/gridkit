package com.medx.processing.util;

import static java.lang.String.format;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.Serializer;

import com.medx.processing.dictionary.DictionaryEntry;

public class XmlUtil {
	private static final Logger log = Logger.getLogger(XmlUtil.class.getCanonicalName());
	
	public static int getMaximumId(Document dictionary) {
		int maxId = -1;
		
		Nodes nodes = dictionary.query("/attributes/attribute[@id]");
		
		for (int i = 0; i < nodes.size(); ++i) {
			Element element = (Element) nodes.get(i);
			
			int curId = Integer.valueOf(element.getAttribute("id").getValue());
			
			if (curId > maxId)
				maxId = curId;
		}
		
		return maxId;
	}
	
	public static void populateDictionary(Document dictionary, List<DictionaryEntry> entries) {
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
		Element attributes = new Element("attributes");
		attributes.addAttribute(new Attribute("version", "1"));
		return new Document(attributes);
	}

	public static int getDictionaryVersion(Document dictionary) {
		return Integer.valueOf(dictionary.getRootElement().getAttribute("version").getValue());
	}
	
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
		
		if (entry.getDescription() != null) {
			Element description = new Element("description");
			description.appendChild(entry.getDescription());
			attribute.appendChild(description);
		}
		
		return attribute;
	}
	
	public static void storeDictionary(Document dictionary, String file) throws IOException {
		FileOutputStream targetOutput = new FileOutputStream(new File(file));
		
		Serializer serializer = new Serializer(targetOutput, "UTF-8");
		serializer.setIndent(4);
		serializer.write(dictionary);
		
		targetOutput.close();
	}
}
