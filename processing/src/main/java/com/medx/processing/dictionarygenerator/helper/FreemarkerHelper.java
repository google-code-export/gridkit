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
		
		Map<String, Object> templateData = prepareTemplateData(dictClass, dictPackage);
		templateData.put("typeDesc", mapTypeDescriptorToView(typeDesc));
		templateData.put("attrDescs", mapAttributeDescriptorsToView(attrDescs));
		
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

	public Map<String, String> mapTypeDescriptorToView(TypeDescriptor desc) {
		Map<String, String> result = new HashMap<String, String>();
		
		result.put("id", String.valueOf(desc.getId()));
		result.put("version", String.valueOf(desc.getVersion()));
		result.put("clazz", desc.getClazz());
		
		return result;
	}
	
	private List<Map<String, String>> mapAttributeDescriptorsToView(List<AttributeDescriptor> attrDescs) {
		List<Map<String, String>> result = new ArrayList<Map<String,String>>();
		
		for(AttributeDescriptor attributeDescriptor : attrDescs)
			result.add(mapAttributeDescriptorToView(attributeDescriptor));
		
		return result;
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
		result.put("clazz", ClassUtil.getCanonicalRawType(desc.getClazz()) + ".class");
		
		return result;
	}
}
