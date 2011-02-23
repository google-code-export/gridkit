package com.medx.processing.dictionary;

import static com.medx.processing.util.MirrorUtil.filterExecutableElements;
import static com.medx.processing.util.MirrorUtil.filterGetters;
import static com.medx.processing.util.MirrorUtil.getEnvOption;
import static com.medx.processing.util.MirrorUtil.mapDictionaryEntries;

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

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.medx.attribute.AttrKey;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@SupportedAnnotationTypes("com.medx.type.annotation.DictType")
@SupportedOptions({"packageCutPrefix", "xmlAddPrefix", "javaAddPrefix", "sourceFolder", "dictionaryFile"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class JavaDictionaryGenerator extends AbstractProcessor{
	private static Logger log = LoggerFactory.getLogger(XmlDictionaryGenerator.class);
	
	private String packageCutPrefix;
	private String xmlAddPrefix;
	private String javaAddPrefix;
	private String sourceFolder;
	
	@Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
    	super.init(processingEnv);
    	
    	packageCutPrefix = getEnvOption("packageCutPrefix", processingEnv, "");
    	xmlAddPrefix = getEnvOption("xmlAddPrefix", processingEnv, "");
    	javaAddPrefix = getEnvOption("javaAddPrefix", processingEnv, "");
    	sourceFolder = getEnvOption("sourceFolder", processingEnv, "");
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
		Configuration cfg = new Configuration();
		cfg.setClassForTemplateLoading(JavaDictionaryGenerator.class, "/freemarker");
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		
		Template temp = cfg.getTemplate("javaDictionary.ftl");
		
		for (TypeElement dictType : elements)
			for (Element clazz : env.getElementsAnnotatedWith(dictType)) {
				List<ExecutableElement> getters = filterGetters(filterExecutableElements(clazz.getEnclosedElements()));
				List<DictionaryEntry> entries = mapDictionaryEntries(getters, 0, packageCutPrefix, xmlAddPrefix);
				
				Map<String, Object> templateData = new HashMap<String, Object>();
				
				List<Map<String, String>> entriesToTemplate = new ArrayList<Map<String,String>>();
				
				for(DictionaryEntry entry : entries)
					entriesToTemplate.add(mapEntryToView(entry));
				
				String dictionaryPackage = getJavaDictionaryPackage((TypeElement) clazz);
				String dictionaryClass = ((TypeElement)clazz).getSimpleName().toString();
				
				templateData.put("entries", entriesToTemplate);
				templateData.put("package", dictionaryPackage);
				templateData.put("attrKeyClass", AttrKey.class.getCanonicalName());
				templateData.put("className", dictionaryClass);
				
				String sourceFile = sourceFolder + '/' + dictionaryPackage.replaceAll("\\.", "/") + '/' + dictionaryClass + ".java";
				
				log.info("Writing file + " + (new File(sourceFile)).getAbsolutePath());
				
				Writer out = new FileWriter(sourceFile);
				temp.process(templateData, out);
				out.flush();
				out.close();
			}
		
		return true;
	}
	
	private String getJavaDictionaryPackage(TypeElement clazz) {
		String packageName = clazz.getQualifiedName().toString();
		
		packageName = packageName.contains(".") ? packageName.substring(0, packageName.lastIndexOf('.')) : "";
		
		if (!packageCutPrefix.isEmpty() && !packageName.startsWith(packageCutPrefix))
			throw new IllegalArgumentException("clazz");
		else
			packageName = packageName.substring(packageCutPrefix.length());

		return javaAddPrefix + packageName;
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
