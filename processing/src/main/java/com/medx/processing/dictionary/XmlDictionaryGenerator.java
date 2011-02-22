package com.medx.processing.dictionary;

import static com.medx.processing.util.MirrorUtil.filterExecutableElements;
import static com.medx.processing.util.MirrorUtil.filterGetters;
import static com.medx.processing.util.MirrorUtil.mapDictionaryEntries;
import static com.medx.processing.util.MirrorUtil.createClassDictionaryEntry;
import static com.medx.processing.util.XmlUtil.storeDictionary;
import static com.medx.processing.util.XmlUtil.createEmptyDictionary;
import static com.medx.processing.util.XmlUtil.populateDictionary;
import static com.medx.processing.util.XmlUtil.getDictionaryVersion;

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

import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

@SupportedAnnotationTypes("com.medx.type.annotation.DictType")
@SupportedOptions({"XmlDictionaryGenerator.cutPrefix", "XmlDictionaryGenerator.addPrefix", "XmlDictionaryGenerator.targetFile"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class XmlDictionaryGenerator extends AbstractProcessor {
	private String cutPrefix;
	private String addPrefix;
	private String targetFile;
	
	@Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
    	super.init(processingEnv);
    	
    	cutPrefix  = processingEnv.getOptions().get("XmlDictionaryGenerator.cutPrefix");
    	addPrefix  = processingEnv.getOptions().get("XmlDictionaryGenerator.addPrefix");
    	targetFile = processingEnv.getOptions().get("XmlDictionaryGenerator.targetFile");
    	
    	cutPrefix = cutPrefix == null ? "" : cutPrefix;
    	addPrefix = addPrefix == null ? "" : addPrefix;
	}
	
	@Override
	public boolean process(Set<? extends TypeElement> elements, RoundEnvironment env) {
		try {
			return processInternal(elements, env);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private boolean processInternal(Set<? extends TypeElement> elements, RoundEnvironment env) throws IOException, ValidityException, ParsingException, SAXException {
		if (elements.size() == 0)
			return true;
		
		Document dictionary = null;
		
		File dictionaryFile = new File(targetFile);
	
		if (dictionaryFile.exists()) {
			Builder parser = new Builder(XMLReaderFactory.createXMLReader(), false);
			dictionary = parser.build(dictionaryFile);
		}
		else {
			dictionary = createEmptyDictionary();
			storeDictionary(dictionary, targetFile);
		}
		
		int version = getDictionaryVersion(dictionary);
		
		List<ExecutableElement> getters = new ArrayList<ExecutableElement>();
		List<DictionaryEntry> entries = new ArrayList<DictionaryEntry>();
		
		for (TypeElement dictType : elements)
			for (Element clazz : env.getElementsAnnotatedWith(dictType)) {
				getters.addAll(filterGetters(filterExecutableElements(clazz.getEnclosedElements())));
				entries.add(createClassDictionaryEntry((TypeElement)clazz, version, cutPrefix, addPrefix));
			}
		
		entries.addAll(mapDictionaryEntries(getters, version, cutPrefix, addPrefix));
		
		populateDictionary(dictionary, entries);
		
		storeDictionary(dictionary, targetFile);

		return true;
	}
}
