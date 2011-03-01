package com.medx.processing.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import com.medx.framework.util.DictUtil;
import com.medx.framework.util.TextUtil;
import com.medx.processing.dictionary.DictionaryEntry;

public class MirrorUtil {
	private static String GETTER_PATTERN  = "get[A-Z].*";
	
	public static String getEnvOption(String option, ProcessingEnvironment processingEnv) {
		String result = processingEnv.getOptions().get(option);
		
		if (result == null)
			throw new RuntimeException("No env option " + option + " was found");
			
		return result;
	}
	
	public static List<ExecutableElement> filterExecutableElements(List<? extends Element> elements) {
		List<ExecutableElement> result = new ArrayList<ExecutableElement>();
		
		for (Element element : elements)
			if (element instanceof ExecutableElement)
				result.add((ExecutableElement)element);
		
		return result;
	}
	
	public static List<ExecutableElement> filterGetters(List<ExecutableElement> elements) {
		List<ExecutableElement> result = new ArrayList<ExecutableElement>();
		
		for (ExecutableElement element : elements)
			if (isGetter(element))
				result.add(element);
		
		return result;
	}
	
	public static boolean isGetter(ExecutableElement element) {
		if (element.getParameters().size() > 0)
			return false;

		TypeKind typeKind = element.getReturnType().getKind();
		
		if (!(typeKind.isPrimitive() || typeKind == TypeKind.DECLARED || typeKind == TypeKind.ARRAY))
			return false;
		
		if (!element.getSimpleName().toString().matches(GETTER_PATTERN))
			return false;
		
		return true;
	}
	
	public static DictionaryEntry createClassDictionaryEntry(TypeElement clazz) {
		DictionaryEntry result = new DictionaryEntry();
		
		result.setName(DictUtil.getAttrName(clazz, "classAttribute"));
		result.setType("java.lang.Class<" + clazz.getQualifiedName() + ">");
		
		return result;
	}
	
	public static DictionaryEntry createAttrDictionaryEntry(ExecutableElement getter) {
		DictionaryEntry result = new DictionaryEntry();
		
		String attrName = TextUtil.getCamelPostfix(getter.getSimpleName().toString());
		result.setName(DictUtil.getAttrName((TypeElement)getter.getEnclosingElement(), attrName));
		
		result.setType(replacePrimitiveType(getter.getReturnType().toString()));
		
		return result;
	}
	
	private static Map<String, Class<?>> primitiveTypeReplacements = new HashMap<String, Class<?>>();
	
	static {
		primitiveTypeReplacements.put("bool", Boolean.class);
		primitiveTypeReplacements.put("byte", Byte.class);
		primitiveTypeReplacements.put("int", Integer.class);
		primitiveTypeReplacements.put("long", Long.class);
		primitiveTypeReplacements.put("char", Character.class);
		primitiveTypeReplacements.put("float", Float.class);
		primitiveTypeReplacements.put("double", Double.class);
	}
	
	private static String replacePrimitiveType(String type) {
		for (Map.Entry<String, Class<?>> replacement : primitiveTypeReplacements.entrySet())
			if (replacement.getKey().equals(type))
				return replacement.getValue().getCanonicalName();
		
		return type;
	}
}
