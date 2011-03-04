package com.medx.processing.dictionarygenerator;

import static com.medx.processing.dictionarygenerator.helper.MirrorHelper.createTypeDescriptor;
import static com.medx.processing.dictionarygenerator.helper.MirrorHelper.filterDictTypes;
import static com.medx.processing.dictionarygenerator.helper.MirrorHelper.filterExecutableElements;
import static com.medx.processing.dictionarygenerator.helper.MirrorHelper.filterGetters;
import static com.medx.processing.dictionarygenerator.helper.MirrorHelper.mapAttributeDescriptors;
import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
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
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.tools.Diagnostic.Kind;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.medx.framework.annotation.JavaDictionary;
import com.medx.framework.annotation.ModelClass;
import com.medx.framework.annotation.ModelPackage;
import com.medx.framework.annotation.XmlDictionary;
import com.medx.framework.dictionary.DictionaryReader;
import com.medx.framework.dictionary.DictionaryWriter;
import com.medx.framework.dictionary.model.AttributeDescriptor;
import com.medx.framework.dictionary.model.Dictionary;
import com.medx.framework.dictionary.model.TypeDescriptor;
import com.medx.processing.dictionarygenerator.helper.DictionaryHelper;
import com.medx.processing.dictionarygenerator.helper.FreemarkerHelper;
import com.medx.processing.util.MessageUtil;

public class ModelPackageProcessor {
	private RoundEnvironment roundEnv;
	private ProcessingEnvironment processingEnv;
	
	private PackageElement modelPackageElement;
	
	private String modelPackageName;
	
	private ModelPackage modelPackage;
	private XmlDictionary xmlDictionary;
	private JavaDictionary javaDictionary;
	
	private Set<String> modelClasses = new HashSet<String>();
	
	private Map<String, TypeElement> typeElements = new HashMap<String, TypeElement>();
	private Map<String, TypeDescriptor> typeDescriptors = new HashMap<String, TypeDescriptor>();
	
	private Map<String, List<ExecutableElement>> attributeElements = new HashMap<String, List<ExecutableElement>>();
	private Map<String, List<AttributeDescriptor>> attributeDescriptors = new HashMap<String, List<AttributeDescriptor>>();
	
	private Dictionary permanentDictionary;
	private Dictionary temporaryDictionary;
	
	private DictionaryHelper permanentDictionaryHelper;
	
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
		
		if (xmlDictionary.permanentPath() == null || xmlDictionary.temporaryPath() == null || xmlDictionary.permanentPath().equals(xmlDictionary.temporaryPath()))
			throw new ModelPackageProcessingException("Illegal XmlDictionary values - " + xmlDictionary.toString(), modelPackageElement, true);
		
		try {
			loadPermanentDictionary();
			createTemporaryDictionary();
		} catch (Exception e) {
			throw new ModelPackageProcessingException("Failed to init dicionaries", e, modelPackageElement, true);
		}
		
		prepareDescriptors();
		populateDictionary();
		
		try {
			storeTemporaryDictionary();
		} catch (Exception e) {
			throw new ModelPackageProcessingException("Failed to store dicionary", e, modelPackageElement, true);
		}
		
		if (javaDictionary == null)
			return;
		
		try {
			initFreemarker();
		} catch (Exception e) {
			throw new ModelPackageProcessingException("Failed to init freemarker", e, modelPackageElement, true);
		}
		
		writeDictionaClasses();
	}
	
	private void prepareDescriptors() {
		Set<? extends Element> allDictTypes = roundEnv.getElementsAnnotatedWith(ModelClass.class);
		
		List<TypeElement> dictTypes = filterDictTypes(allDictTypes, modelPackageName);
		
		for (TypeElement dictType : dictTypes) {
			String className = dictType.getQualifiedName().toString();
			
			modelClasses.add(className);

			typeElements.put(className, dictType);
			typeDescriptors.put(className, createTypeDescriptor(dictType));
			
			List<ExecutableElement> methods = filterExecutableElements(dictType.getEnclosedElements());
			List<ExecutableElement> getters = filterGetters(methods);
			
			attributeElements.put(className, getters);
			attributeDescriptors.put(className, mapAttributeDescriptors(getters, modelPackageName, modelPackage));
		}
	}
	
	private static class PopulateDictionaryException extends Exception {
		private static final long serialVersionUID = 2278038533169896184L;
	}
	
	private void populateDictionary() throws ModelPackageProcessingException {
		int nextId = permanentDictionaryHelper.getNextId(xmlDictionary.startId());
		
		boolean hasErrors = false;
		
		try {
			nextId = populateTypeDescriptors(nextId);
		} catch (PopulateDictionaryException e) {
			hasErrors = true;
		}
		
		for (String clazz : modelClasses)
			try {
				nextId = populateAttributeDescriptors(clazz, nextId);
			} catch (PopulateDictionaryException e) {
				hasErrors = true;
			}
		
		if (hasErrors)
			throw new ModelPackageProcessingException(modelPackageElement, false);
	}
	
	private int populateTypeDescriptors(int nextId) throws PopulateDictionaryException {
		Set<String> registeredTypes = permanentDictionaryHelper.getRegisteredTypes();
		
		for (Map.Entry<String, TypeDescriptor> descEntry : typeDescriptors.entrySet()) {
			TypeDescriptor oldDesc = permanentDictionaryHelper.containsTypeDescriptor(descEntry.getValue());
			
			if (oldDesc == null) {
				descEntry.getValue().setId(nextId++);
				descEntry.getValue().setVersion(temporaryDictionary.getVersion());
				
				temporaryDictionary.getTypeDescriptors().add(descEntry.getValue());
			}
			else {
				descEntry.setValue(oldDesc);
				registeredTypes.remove(oldDesc.getJavaClassName());
			}
		}
		
		if (!registeredTypes.isEmpty()) {
			for (String type : registeredTypes) {
				String message = format("Class '%s' was removed from source code but presented in permanent dictionary", type);
				processingEnv.getMessager().printMessage(Kind.ERROR, message, modelPackageElement);
			}
			
			throw new PopulateDictionaryException();
		}
		
		return nextId;
	}
	
	private int populateAttributeDescriptors(String clazz, int nextId) throws PopulateDictionaryException {
		Set<String> registeredAttributes = permanentDictionaryHelper.getRegisteredAttributes(modelPackage, modelPackageName, clazz);
		
		List<AttributeDescriptor> oldDescs = new ArrayList<AttributeDescriptor>();
		
		boolean hasErrors = false;
		
		int i = 0;
		for (Iterator<AttributeDescriptor> iter = attributeDescriptors.get(clazz).iterator(); iter.hasNext(); ++i) {
			AttributeDescriptor desc = iter.next();

			AttributeDescriptor oldDesc = permanentDictionaryHelper.containsAttributeDescriptor(desc);

			if (oldDesc == null) {
				desc.setId(nextId++);
				desc.setVersion(temporaryDictionary.getVersion());
				
				temporaryDictionary.getAttributeDescriptors().add(desc);
			}
			else {
				if (!desc.getClazz().equals(oldDesc.getClazz())) {
					hasErrors = true;
					String message = format("This attribute has another class in permanent dictionary ('%s')", oldDesc.getClazz());
					processingEnv.getMessager().printMessage(Kind.ERROR, message, attributeElements.get(clazz).get(i));
				}
				
				iter.remove();
				oldDescs.add(oldDesc);
				registeredAttributes.remove(oldDesc.getName());
			}
		}
		
		attributeDescriptors.get(clazz).addAll(oldDescs);
		
		if (!registeredAttributes.isEmpty()) {
			hasErrors = true;
			
			for (String attribute : registeredAttributes) {
				String message = format("Attribute '%s' was removed from source code but presented in permanent dictionary", attribute);
				processingEnv.getMessager().printMessage(Kind.ERROR, message, typeElements.get(clazz));
			}
		}
		
		if (hasErrors)
			throw new PopulateDictionaryException();
		
		return nextId;
	}
	
	private void initFreemarker() throws IOException {
		freemarkerHelper = new FreemarkerHelper(processingEnv.getFiler(), modelPackageName, javaDictionary);
	}
	
	private void writeDictionaClasses() {
		for (String clazz : modelClasses) {
			try {
				freemarkerHelper.writeJavaClass(clazz, typeDescriptors.get(clazz), attributeDescriptors.get(clazz));
			} catch (Exception e) {
				processingEnv.getMessager().printMessage(Kind.WARNING, MessageUtil.createMessage("Failed to write dictionary file", e), typeElements.get(clazz));
			}
		}
	}
	
	private void createTemporaryDictionary() {
		temporaryDictionary = DictionaryHelper.createEmptyDictionary(permanentDictionary.getVersion() + 1);
	}
	
	private void loadPermanentDictionary() throws IOException, JAXBException, SAXException {
		DictionaryReader dictionaryReader = new DictionaryReader();
		
		try {
			FileObject fileObject = processingEnv.getFiler().getResource(StandardLocation.SOURCE_OUTPUT, "", xmlDictionary.permanentPath());
			InputStream inputStream = fileObject.openInputStream();
			permanentDictionary = dictionaryReader.readDictionary(inputStream);
			try {inputStream.close();} catch (IOException e) {}
		}
		catch (IOException e) {
			FileObject fileObject = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "", xmlDictionary.permanentPath());
			permanentDictionary = DictionaryHelper.createEmptyDictionary(0);
			storeDictionary(fileObject, permanentDictionary);
		}
		
		permanentDictionaryHelper = new DictionaryHelper(permanentDictionary);
	}
	
	private static void storeDictionary(FileObject fileObject, Dictionary dictionary) throws IOException, JAXBException {
		DictionaryWriter dictionaryWriter = new DictionaryWriter();
		
		Writer writer = fileObject.openWriter();
		
		try {
			dictionaryWriter.writeDictionary(writer, dictionary);
		}
		finally {
			try {writer.close();} catch (IOException e) {}
		}
	}
	
	private void storeTemporaryDictionary() throws JAXBException, IOException {
		storeDictionary(processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "", xmlDictionary.temporaryPath(), (Element)null), temporaryDictionary);
	}
}
