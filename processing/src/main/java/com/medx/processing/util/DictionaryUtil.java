package com.medx.processing.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;

public class DictionaryUtil {
	private static Logger log = LoggerFactory.getLogger(DictionaryUtil.class);
	
	public static Document createDictionary() {
		Element attributes = new Element("attributes");
		attributes.addAttribute(new Attribute("version", "1"));
		return new Document(attributes);
	}
	
	public static Document loadDictionary(File file, Builder parser) throws ValidityException, ParsingException, IOException {
		if (!file.exists())
			return null;
		
		Document dictionary = parser.build(file);
		
		if (!validateDictionary(dictionary)) {
			log.warn(format("Dictionary in file '%s' is not valid", file.getAbsoluteFile()));
			return null;
		}
		
		return dictionary;
	}
	
	public static Document loadOrCreateDictionary(File file, Builder parser) throws ValidityException, ParsingException, IOException {
		Document dictionary = loadDictionary(file, parser);
		
		return dictionary == null ? createDictionary() : dictionary;
	}
	
	public static boolean isDictionaryWithVersion(Document dictionary) {
		return dictionary.getRootElement().getAttribute("version") != null;
	}
	
	public static int getDictionaryVersion(Document dictionary) {
		return Integer.valueOf(dictionary.getRootElement().getAttribute("version").getValue());
	}
	
	public static void storeDictionary(Document dictionary, File file) throws IOException {
		FileOutputStream targetOutput = new FileOutputStream(file);
		
		Serializer serializer = new Serializer(targetOutput, "UTF-8");
		serializer.setIndent(4);
		serializer.write(dictionary);
		
		targetOutput.close();
	}
	
	public static boolean validateDictionary(Document dictionary) {
		if (dictionary == null)
			return false;
		
		Element root = dictionary.getRootElement();
		
		if (dictionary == null || !"attributes".equals(root.getLocalName()))
			return false;
		
		if (dictionary.query("/attributes/attribute[not(@id and name and type)]").size() > 0)
			return false;
		
		return true;
	}
	
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
}
