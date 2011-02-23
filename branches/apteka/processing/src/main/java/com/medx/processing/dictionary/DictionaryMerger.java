package com.medx.processing.dictionary;

import static com.medx.processing.util.DictionaryUtil.getDictionaryVersion;
import static com.medx.processing.util.DictionaryUtil.loadOrCreateDictionary;
import static com.medx.processing.util.DictionaryUtil.loadDictionary;
import static com.medx.processing.util.DictionaryUtil.isDictionaryWithVersion;
import static com.medx.processing.util.DictionaryUtil.storeDictionary;
import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

public class DictionaryMerger {
	private static final Logger log = LoggerFactory.getLogger(DictionaryMerger.class);
	
	public static void main(String[] args) throws SAXException, ValidityException, ParsingException, IOException {
		Builder parser = new Builder(XMLReaderFactory.createXMLReader(), false);
		
		File destinationFile = new File(args[0]);
		Document destinationDictionary = loadOrCreateDictionary(destinationFile, parser);
		
		String sources[] = Arrays.copyOfRange(args, 1, args.length);
		
		for (String source : sources)
			mergeDictionary(loadDictionary(new File(source), parser), destinationDictionary);
		
		storeDictionary(destinationDictionary, destinationFile);
	}
	
	public static void mergeDictionary(Document source, Document destination) {
		int defaultVersion = getDefaultVersion(source, destination);
		
		Nodes nodes = source.query("/attributes/attribute[@id and name]");
		
		for (int i = 0; i < nodes.size(); ++i) {
			Element element = (Element) nodes.get(i);
			
			String id = element.getAttributeValue("id");
			String name = element.getChildElements("name").get(0).getValue();
			
			if (destination.query(format("/attributes/attribute[id = '%s' or name = '%s']", id, name)).size() > 0)
				log.warn(format("Attribute with id = '%s' or name = '%s' already presented in ", id, name));
			else {
				if (element.getAttribute("version") == null)
					element.addAttribute(new Attribute("version", String.valueOf(defaultVersion)));
				
				destination.getRootElement().appendChild(element.copy());
			}
		}
	}
	
	public static int getDefaultVersion(Document source, Document destination) {
		if (isDictionaryWithVersion(source))
			return getDictionaryVersion(source);
		else if (isDictionaryWithVersion(destination))
			return getDictionaryVersion(destination);
		else
			throw new IllegalArgumentException("Neither source or destination dictionary has default version attribute");
	}
}
