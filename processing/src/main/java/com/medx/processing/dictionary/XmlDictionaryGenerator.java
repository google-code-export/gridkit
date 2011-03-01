package com.medx.processing.dictionary;

import static com.medx.processing.util.DictionaryUtil.getDictionaryVersion;
import static com.medx.processing.util.DictionaryUtil.getMaximumId;
import static com.medx.processing.util.DictionaryUtil.loadOrCreateDictionary;
import static com.medx.processing.util.DictionaryUtil.storeDictionary;
import static com.medx.processing.util.MirrorUtil.*;
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
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.medx.framework.annotation.DictType;
import com.medx.framework.annotation.ModelPackage;
import com.medx.framework.dictionary.model.AttributeDescriptor;
import com.medx.framework.dictionary.model.TypeDescriptor;
import com.medx.framework.util.DictUtil;

@SupportedAnnotationTypes("com.medx.framework.annotation.ModelPackage")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class XmlDictionaryGenerator extends AbstractProcessor {
	private static Logger log = LoggerFactory.getLogger(XmlDictionaryGenerator.class);
	
	private final static String GETTER_PATTERN  = "get[A-Z].*";
	
	@Override
	public boolean process(Set<? extends TypeElement> elements, RoundEnvironment env) {
		if (elements.size() == 0) {
			System.out.println("TODO fix - XmlDictionaryGenerator.empty");
			return false;
		}
		
		try {
			return processInternal(elements, env);
		} catch (Exception e) {
			log.warn("Exception during xml dictionary generation", e);
			throw new RuntimeException(e);
		}
	}
	
	private boolean processInternal(Set<? extends TypeElement> elements, RoundEnvironment env) {
		for (TypeElement modelPackage : elements)
			for (Element packet : env.getElementsAnnotatedWith(modelPackage))
				processModelPackage((PackageElement)packet, env);
		
		return false;
	}

	private void processModelPackage(PackageElement modelPackage, RoundEnvironment env) {
		Set<? extends Element> allDictTypes = env.getElementsAnnotatedWith(DictType.class);
		
		List<TypeElement> dictTypes = null; //filterDictTypes(allDictTypes, modelPackage);
		
		List<ExecutableElement> getters = new ArrayList<ExecutableElement>();
		
		for (TypeElement dictType : dictTypes)
			getters.addAll(filterGetters(filterExecutableElements(dictType.getEnclosedElements())));
	}
	
	/*
	public static AttributeDescriptor createAttributeDescriptor(TypeElement clazz) {
		AttributeDescriptor result = new AttributeDescriptor();
		
		result.setName(DictUtil.getAttrName(clazz, "classAttribute"));
		result.
		
		return result;
	}
	*/
	
	/*
	private boolean processInternal(Set<? extends TypeElement> elements, RoundEnvironment env) throws IOException, ValidityException, ParsingException, SAXException {
		File dictionaryFileDesc = new File(dictionaryFile);
	
		Document dictionary = loadOrCreateDictionary(dictionaryFileDesc);
		
		int dictionaryVersion = getDictionaryVersion(dictionary);
		
		List<ExecutableElement> getters = new ArrayList<ExecutableElement>();
		List<DictionaryEntry> entries = new ArrayList<DictionaryEntry>();
		
		for (TypeElement dictType : elements)
			for (Element clazz : env.getElementsAnnotatedWith(dictType)) {
				getters.addAll(filterGetters(filterExecutableElements(clazz.getEnclosedElements())));
				
				DictionaryEntry classEntry = createClassDictionaryEntry((TypeElement)clazz);
				classEntry.setVersion(dictionaryVersion);
				
				entries.add(classEntry);
			}
		
		entries.addAll(mapTypeDictionaryEntries(getters, dictionaryVersion));
		
		populateDictionary(dictionary, entries);
		
		storeDictionary(dictionary, dictionaryFileDesc);

		return false;
	}
	*/
	/*
	public static List<DictionaryEntry> mapTypeDictionaryEntries(List<ExecutableElement> getters, int version) {
		List<DictionaryEntry> result = new ArrayList<DictionaryEntry>();
		
		for (ExecutableElement getter : getters) {
			DictionaryEntry entry = createAttrDictionaryEntry(getter);
			entry.setVersion(version);
			result.add(entry);
		}
		
		return result;
	}
	
	public void populateDictionary(Document dictionary, List<DictionaryEntry> entries) {
		List<DictionaryEntry> entriesToPopulate = new ArrayList<DictionaryEntry>();
		
		for (DictionaryEntry entry : entries) {
			if (dictionary.query(format("/attributes/attribute[name='%s']", entry.getName())).size() == 0)
				entriesToPopulate.add(entry);
			else
				log.warn(format("Failed to add dictionary entry with name '%s' because this name is already in use", entry.getName()));
		}
		
		int id = getMaximumId(dictionary, startId);
		
		for (DictionaryEntry entry : entriesToPopulate) {
			entry.setId(++id);
			dictionary.getRootElement().appendChild(toXML(entry));
		}
	}
	*/
}
