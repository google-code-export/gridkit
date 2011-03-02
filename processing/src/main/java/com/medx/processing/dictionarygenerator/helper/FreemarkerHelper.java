package com.medx.processing.dictionarygenerator.helper;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;

import com.medx.framework.annotation.JavaDictionary;
import com.medx.framework.attribute.AttrKey;
import com.medx.framework.dictionary.model.AttributeDescriptor;
import com.medx.framework.dictionary.model.TypeDescriptor;
import com.medx.framework.type.TypeKey;
import com.medx.framework.util.ClassUtil;
import com.medx.framework.util.DictUtil;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreemarkerHelper {
	private final Template dictionaryTemplate;
	
	private Filer filer;
	
	private String modelPackageName;
	private JavaDictionary javaDictionary;
	
	public FreemarkerHelper(Filer filer, String modelPackageName, JavaDictionary javaDictionary) throws IOException {
		this.filer = filer;
		this.modelPackageName = modelPackageName;
		this.javaDictionary = javaDictionary;
		
		Configuration configuration = new Configuration();
		
		configuration.setClassForTemplateLoading(FreemarkerHelper.class, "/freemarker");
		configuration.setObjectWrapper(new DefaultObjectWrapper());
		
		dictionaryTemplate = configuration.getTemplate("javaDictionary.ftl");
	}
	
	public void writeJavaClass(String className, TypeDescriptor typeDesc, List<AttributeDescriptor> attrDescs) throws IOException, TemplateException {
		String dictClass = ClassUtil.getSimpleClassName(className);
		String dictPackage = DictUtil.getJavaDictionaryPackage(ClassUtil.getClassPackage(className), modelPackageName, javaDictionary);
		String dictClassFullName = ClassUtil.getFullClassName(dictClass, dictPackage);
		
		List<Map<String, String>> attrDescsToTemplate = new ArrayList<Map<String,String>>();
		
		for(AttributeDescriptor attributeDescriptor : attrDescs)
			attrDescsToTemplate.add(mapAttributeDescriptorToView(attributeDescriptor));
		
		Map<String, Object> templateData = prepareTemplateData(dictClass, dictPackage);
		templateData.put("attrDescs", attrDescsToTemplate);
		
		Writer writer = filer.createSourceFile(dictClassFullName).openWriter();
		
		dictionaryTemplate.process(templateData, writer);
		writer.flush();
		writer.close();
	}
	
	private Map<String, Object> prepareTemplateData(String dictionaryClass, String dictionaryPackage) {
		Map<String, Object> templateData = new HashMap<String, Object>();

		templateData.put("package", dictionaryPackage);
		
		templateData.put("attrKeyClass", AttrKey.class.getCanonicalName());
		templateData.put("typeKeyClass", TypeKey.class.getCanonicalName());
		
		templateData.put("className", dictionaryClass);
		
		return templateData;
	}

	private Map<String, String> mapAttributeDescriptorToView(AttributeDescriptor desc) {
		Map<String, String> result = new HashMap<String, String>();
		
		String entryName = desc.getName();
		
		result.put("id", String.valueOf(desc.getId()));
		result.put("name", "\"" + entryName + "\"");
		result.put("varName", entryName.substring(entryName.lastIndexOf('.') + 1));
		result.put("version", String.valueOf(desc.getVersion()));
		result.put("description", "\"" + desc.getDescription() + "\"");
		result.put("type", desc.getClazz());
		result.put("clazz", getRawClass(desc.getClazz()));
		
		return result;
	}
	
	private static String getRawClass(String clazz) {
		int index = clazz.indexOf('<');
		
		if (index != -1) {
			int lastIndex = clazz.lastIndexOf('>');
			clazz = clazz.substring(0, index) + clazz.substring(lastIndex + 1);
		}
		
		return clazz + ".class";
	}
}
