package com.medx.processing.dictionary;

import static com.medx.processing.util.MirrorUtil.filterExecutableElements;
import static com.medx.processing.util.MirrorUtil.filterGetters;
import static com.medx.processing.util.MirrorUtil.getEnvOption;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.medx.framework.attribute.AttrKey;
import com.medx.framework.util.DictUtil;
import com.medx.processing.util.DictionaryUtil;
import com.medx.processing.util.MirrorUtil;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@SupportedAnnotationTypes("com.medx.type.annotation.DictType")
@SupportedOptions({"sourceFolder", "dictionaryFile"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class JavaDictionaryGenerator extends AbstractProcessor{
	private static Logger log = LoggerFactory.getLogger(XmlDictionaryGenerator.class);
	
	private String dictionaryFile;
	private String sourceFolder;
	
	private Template dictionaryTemplate;
	
	private Document dictionary;
	
	public JavaDictionaryGenerator() throws IOException {
		Configuration configuration = new Configuration();
		
		configuration.setClassForTemplateLoading(JavaDictionaryGenerator.class, "/freemarker");
		configuration.setObjectWrapper(new DefaultObjectWrapper());
		
		dictionaryTemplate = configuration.getTemplate("javaDictionary.ftl");
	}
	
	@Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
    	super.init(processingEnv);
    	
    	sourceFolder = getEnvOption("sourceFolder", processingEnv);
    	dictionaryFile = getEnvOption("dictionaryFile", processingEnv);
    	
    	try {
			dictionary = DictionaryUtil.loadDictionary(new File(dictionaryFile));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public boolean process(Set<? extends TypeElement> elements, RoundEnvironment env) {
		if (elements.size() == 0) {
			System.out.println("TODO fix : JavaDictionaryGenerator.empty");
			return false;
		}
		
		try {
			return processInternal(elements, env);
		} catch (Exception e) {
			log.warn("Exception during annotation processing", e);
			throw new RuntimeException(e);
		}
	}
	
	private boolean processInternal(Set<? extends TypeElement> elements, RoundEnvironment env) throws IOException, ValidityException, ParsingException, SAXException, TemplateException {
		for (TypeElement dictType : elements)
			for (Element clazz : env.getElementsAnnotatedWith(dictType)) {
				List<ExecutableElement> getters = filterGetters(filterExecutableElements(clazz.getEnclosedElements()));
				List<DictionaryEntry> entries = createDictionaryEntries(getters);
				
				List<Map<String, String>> entriesToTemplate = new ArrayList<Map<String,String>>();
				
				for(DictionaryEntry entry : entries)
					entriesToTemplate.add(mapEntryToView(entry));
				
				Map<String, Object> templateData = prepareTemplateData((TypeElement)clazz);
				templateData.put("entries", entriesToTemplate);
				
				String sourceFile = getSourceFile((TypeElement) clazz);
				
				log.info("Writing file " + (new File(sourceFile).getAbsolutePath()));
				
				Writer out = new FileWriter(sourceFile);
				dictionaryTemplate.process(templateData, out);
				out.flush();
				out.close();
			}
		
		return true;
	}
	
	private List<DictionaryEntry> createDictionaryEntries(List<ExecutableElement> getters) {
		List<DictionaryEntry> result = new ArrayList<DictionaryEntry>();
		
		for (ExecutableElement getter : getters) {
			DictionaryEntry entry = MirrorUtil.createAttrDictionaryEntry(getter);
			
			entry.setId(Integer.valueOf(DictionaryUtil.getAttributeSign(dictionary, entry.getName(), "id")));
			entry.setVersion(Integer.valueOf(DictionaryUtil.getAttributeSign(dictionary, entry.getName(), "version")));
			
			result.add(entry);
		}
		
		return result;
	}
	
	private Map<String, Object> prepareTemplateData(TypeElement clazz) {
		Map<String, Object> templateData = new HashMap<String, Object>();
		
		String dictionaryPackage = DictUtil.getJavaDictionaryPackage(clazz);
		String dictionaryClass = ((TypeElement)clazz).getSimpleName().toString();
		
		templateData.put("package", dictionaryPackage);
		templateData.put("attrKeyClass", AttrKey.class.getCanonicalName());
		templateData.put("className", dictionaryClass);
		
		return templateData;
	}
	
	private String getSourceFile(TypeElement clazz) {
		String dictionaryPackage = DictUtil.getJavaDictionaryPackage(clazz);
		String dictionaryClass = ((TypeElement)clazz).getSimpleName().toString();
		
		return sourceFolder + '/' + dictionaryPackage.replaceAll("\\.", "/") + '/' + dictionaryClass + ".java";
	}
	
	private Map<String, String> mapEntryToView(DictionaryEntry entry) {
		Map<String, String> result = new HashMap<String, String>();
		
		String entryName = entry.getName();
		
		result.put("id", String.valueOf(entry.getId()));
		result.put("name", "\"" + entryName + "\"");
		result.put("varName", entryName.substring(entryName.lastIndexOf('.') + 1));
		result.put("version", String.valueOf(entry.getVersion()));
		result.put("description", "\"" + entry.getDescription() + "\"");
		result.put("type", entry.getType());
		result.put("clazz", getRawClass(entry.getType()));
		
		return result;
	}
	
	private static String getRawClass(String clazz) {
		if (clazz.contains("<"))
			clazz = clazz.substring(0, clazz.indexOf('<'));
		
		return clazz + ".class";
	}
}
