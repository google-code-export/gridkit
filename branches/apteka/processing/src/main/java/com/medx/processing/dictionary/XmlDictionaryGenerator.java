package com.medx.processing.dictionary;

import static com.medx.processing.util.DictionaryUtil.getDictionaryVersion;
import static com.medx.processing.util.DictionaryUtil.getMaximumId;
import static com.medx.processing.util.DictionaryUtil.isDictionaryWithVersion;
import static com.medx.processing.util.DictionaryUtil.loadOrCreateDictionary;
import static com.medx.processing.util.DictionaryUtil.storeDictionary;
import static com.medx.processing.util.MirrorUtil.createClassDictionaryEntry;
import static com.medx.processing.util.MirrorUtil.filterExecutableElements;
import static com.medx.processing.util.MirrorUtil.filterGetters;
import static com.medx.processing.util.MirrorUtil.mapDictionaryEntries;
import static com.medx.processing.util.MirrorUtil.getEnvOption;
import static com.medx.processing.util.XmlUtil.toXML;
import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

@SupportedAnnotationTypes("com.medx.type.annotation.DictType")
@SupportedOptions({"XmlDictionaryGenerator.cutPrefix", "XmlDictionaryGenerator.addPrefix", "XmlDictionaryGenerator.targetFile"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class XmlDictionaryGenerator extends AbstractProcessor {
	private static Logger log = LoggerFactory.getLogger(XmlDictionaryGenerator.class);
	
	private String cutPrefix;
	private String addPrefix;
	private String targetFile;
	
	@Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
    	super.init(processingEnv);
    	
    	cutPrefix  = getEnvOption("XmlDictionaryGenerator.cutPrefix", processingEnv, "");
    	addPrefix  = getEnvOption("XmlDictionaryGenerator.addPrefix", processingEnv, "");
    	targetFile = getEnvOption("XmlDictionaryGenerator.targetFile", processingEnv, null);
	}
	
	@Override
	public boolean process(Set<? extends TypeElement> elements, RoundEnvironment env) {
		try {
			return processInternal(elements, env);
		} catch (Exception e) {
			log.warn("Exception during annotation processing", e);
			throw new RuntimeException(e);
		}
	}
	
	private boolean processInternal(Set<? extends TypeElement> elements, RoundEnvironment env) throws IOException, ValidityException, ParsingException, SAXException {
		if (elements.size() == 0)
			return true;

		File dictionaryFile = new File(targetFile);
	
		Document dictionary = loadOrCreateDictionary(dictionaryFile, new Builder(XMLReaderFactory.createXMLReader(), false));
		
		int dictionaryVersion = isDictionaryWithVersion(dictionary) ? getDictionaryVersion(dictionary) : 1;
		
		List<ExecutableElement> getters = new ArrayList<ExecutableElement>();
		List<DictionaryEntry> entries = new ArrayList<DictionaryEntry>();
		
		for (TypeElement dictType : elements)
			for (Element clazz : env.getElementsAnnotatedWith(dictType)) {
				getters.addAll(filterGetters(filterExecutableElements(clazz.getEnclosedElements())));
				entries.add(createClassDictionaryEntry((TypeElement)clazz, dictionaryVersion, cutPrefix, addPrefix));
			}
		
		entries.addAll(mapDictionaryEntries(getters, dictionaryVersion, cutPrefix, addPrefix));
		
		populateDictionary(dictionary, entries);
		
		storeDictionary(dictionary, dictionaryFile);

		return true;
	}
	
	public static void populateDictionary(Document dictionary, List<DictionaryEntry> entries) {
		List<DictionaryEntry> entriesToPopulate = new ArrayList<DictionaryEntry>();
		
		for (DictionaryEntry entry : entries) {
			if (dictionary.query(format("/attributes/attribute[name='%s']", entry.getName())).size() == 0)
				entriesToPopulate.add(entry);
			else
				log.warn(format("Failed to add dictionary entry with name '%s' because this name is already in use", entry.getName()));
		}
		
		int id = getMaximumId(dictionary);
		
		for (DictionaryEntry entry : entriesToPopulate) {
			entry.setId(++id);
			dictionary.getRootElement().appendChild(toXML(entry));
		}
	}
}
