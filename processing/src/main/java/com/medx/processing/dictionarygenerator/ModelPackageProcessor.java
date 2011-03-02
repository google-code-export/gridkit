package com.medx.processing.dictionarygenerator;

import static com.medx.processing.dictionarygenerator.helper.MirrorHelper.createTypeDescriptor;
import static com.medx.processing.dictionarygenerator.helper.MirrorHelper.filterDictTypes;
import static com.medx.processing.dictionarygenerator.helper.MirrorHelper.filterExecutableElements;
import static com.medx.processing.dictionarygenerator.helper.MirrorHelper.filterGetters;
import static com.medx.processing.dictionarygenerator.helper.MirrorHelper.mapAttributeDescriptors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.medx.framework.annotation.DictType;
import com.medx.framework.annotation.JavaDictionary;
import com.medx.framework.annotation.ModelPackage;
import com.medx.framework.annotation.XmlDictionary;
import com.medx.framework.dictionary.DictionaryReader;
import com.medx.framework.dictionary.DictionaryWriter;
import com.medx.framework.dictionary.model.AttributeDescriptor;
import com.medx.framework.dictionary.model.Dictionary;
import com.medx.framework.dictionary.model.TypeDescriptor;
import com.medx.processing.dictionarygenerator.helper.DictionaryHelper;
import com.medx.processing.dictionarygenerator.helper.FreemarkerHelper;

public class ModelPackageProcessor {
	private RoundEnvironment roundEnv;
	private ProcessingEnvironment processingEnv;
	
	private PackageElement modelPackageElement;
	
	private String modelPackageName;
	
	private ModelPackage modelPackage;
	private XmlDictionary xmlDictionary;
	private JavaDictionary javaDictionary;
	
	private Set<String> modelClasses = new HashSet<String>();
	private Map<String, TypeDescriptor> typeDescriptors = new HashMap<String, TypeDescriptor>();
	private Map<String, List<AttributeDescriptor>> attributeDescriptors = new HashMap<String, List<AttributeDescriptor>>();
	
	private Dictionary dictionary;
	private DictionaryHelper dictionaryHelper;
	
	private FreemarkerHelper freemarkerHelper;
	
	public ModelPackageProcessor(PackageElement modelPackageElement, RoundEnvironment roundEnv, ProcessingEnvironment processingEnv) {
		this.roundEnv = roundEnv;
		this.processingEnv = processingEnv;
		
		this.modelPackageElement = modelPackageElement;

		this.modelPackageName = modelPackageElement.getQualifiedName().toString();
		
		this.modelPackage = modelPackageElement.getAnnotation(ModelPackage.class);
		this.xmlDictionary = modelPackageElement.getAnnotation(XmlDictionary.class);
		this.javaDictionary = modelPackageElement.getAnnotation(JavaDictionary.class);
	}

	public void process() throws ModelPackageProcessingException {
		if (xmlDictionary == null)
			return;
		
		prepareDescriptors();
		
		try {
			loadDictionary();
		} catch (Exception e) {
			throw new ModelPackageProcessingException("Failed to load dicionary", e, modelPackageElement);
		}
		
		populateDictionary();
		
		try {
			storeDictionary();
		} catch (Exception e) {
			throw new ModelPackageProcessingException("Failed to store dicionary", e, modelPackageElement);
		}
		
		if (javaDictionary == null)
			return;
		
		try {
			freemarkerHelper = new FreemarkerHelper(processingEnv.getFiler(), modelPackageName, javaDictionary);
		} catch (Exception e) {
			throw new ModelPackageProcessingException("Failed to init freemarker", e, modelPackageElement);
		}
		
		writeDictionaClasses();
	}
	
	private void prepareDescriptors() {
		Set<? extends Element> allDictTypes = roundEnv.getElementsAnnotatedWith(DictType.class);
		
		List<TypeElement> dictTypes = filterDictTypes(allDictTypes, modelPackageName);
		
		for (TypeElement dictType : dictTypes) {
			String className = dictType.getQualifiedName().toString();
			
			modelClasses.add(className);
			
			typeDescriptors.put(className, createTypeDescriptor(dictType));
			
			List<ExecutableElement> methods = filterExecutableElements(dictType.getEnclosedElements());
			List<ExecutableElement> getters = filterGetters(methods);
			
			attributeDescriptors.put(className, mapAttributeDescriptors(getters, modelPackageName, modelPackage));
		}
	}
	
	private void populateDictionary() {
		int nextId = dictionaryHelper.getNextId(xmlDictionary.startId());
		nextId = populateTypeDescriptors(nextId);
		nextId = populateAttributeDescriptors(nextId);
	}
	
	private int populateTypeDescriptors(int nextId) {
		for (Map.Entry<String, TypeDescriptor> descEntry : typeDescriptors.entrySet()) {
			TypeDescriptor oldDesc = dictionaryHelper.addTypeDescriptor(descEntry.getValue());
			
			if (oldDesc != descEntry.getValue()) {
				descEntry.setValue(oldDesc);
				//TODO generate warning if different types
			}
			else {
				descEntry.getValue().setId(nextId++);
				descEntry.getValue().setVersion(dictionary.getVersion());
			}
		}
		
		return nextId;
	}
	
	private int populateAttributeDescriptors(int nextId) {
		for (List<AttributeDescriptor> descList : attributeDescriptors.values()) {
			
			List<AttributeDescriptor> oldDescs = new ArrayList<AttributeDescriptor>();
			
			for (Iterator<AttributeDescriptor> iter = descList.iterator(); iter.hasNext(); ) {
				AttributeDescriptor desc = iter.next();
				
				AttributeDescriptor oldDesc = dictionaryHelper.addAttributeDescriptor(desc);
				
				if (oldDesc != desc) { 
					iter.remove();
					oldDescs.add(oldDesc);
					//TODO generate warning if different types
				}
				else {
					desc.setId(nextId++);
					desc.setVersion(dictionary.getVersion());
				}
			}
			
			descList.addAll(oldDescs);
		}
		
		return nextId;
	}
	
	private void writeDictionaClasses() {
		for (String clazz : modelClasses) {
			try {
				freemarkerHelper.writeJavaClass(clazz, typeDescriptors.get(clazz), attributeDescriptors.get(clazz));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void loadDictionary() throws JAXBException, SAXException, IOException {
		DictionaryReader dictionaryReader = new DictionaryReader();
		
		if ((new File(xmlDictionary.path()).exists()))
			dictionary = dictionaryReader.readDictionary(xmlDictionary.path());
		else
			dictionary = DictionaryHelper.createEmptyDictionary(1);
		
		dictionaryHelper = new DictionaryHelper(dictionary);
	}
	
	private void storeDictionary() throws JAXBException {
		DictionaryWriter dictionaryWriter = new DictionaryWriter();
		dictionaryWriter.writeDictionary(xmlDictionary.path(), dictionary);
	}
}
