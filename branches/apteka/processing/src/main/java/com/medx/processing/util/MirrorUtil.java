package com.medx.processing.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import com.medx.processing.dictionary.DictionaryEntry;

public class MirrorUtil {
	private static String GETTER_PATTERN  = "get[A-Z].*";
	
	public static String getEnvOption(String option, ProcessingEnvironment processingEnv, String defaultValue) {
		String result = processingEnv.getOptions().get(option);
		return result == null ? defaultValue : result;
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
	
	public static List<DictionaryEntry> mapDictionaryEntries(List<ExecutableElement> getters, int version, String cutPrefix, String addPrefix) {
		List<DictionaryEntry> result = new ArrayList<DictionaryEntry>();
		
		for (ExecutableElement getter : getters)
			result.add(createDictionaryEntry(getter, version, cutPrefix, addPrefix));
		
		return result;
	}

	private static String getAttributeName(String simpleName, TypeElement clazz, String cutPrefix, String addPrefix) {
		simpleName = Character.toLowerCase(simpleName.charAt(3)) + simpleName.substring(4);
		
		String className = clazz.getQualifiedName().toString();
		
		if (!cutPrefix.isEmpty() && !className.startsWith(cutPrefix))
			throw new IllegalArgumentException("cutPrefix");
		
		return (addPrefix.isEmpty() ? "" : addPrefix + ".") + className.substring(cutPrefix.length() + 1) + "." + simpleName;
	}
	
	public static DictionaryEntry createClassDictionaryEntry(TypeElement clazz, int version, String cutPrefix, String addPrefix) {
		DictionaryEntry result = new DictionaryEntry();
		
		result.setVersion(version);
		result.setName(getAttributeName("getClassAttribute", clazz, cutPrefix, addPrefix));
		result.setType("java.lang.Class<" + clazz.getQualifiedName() + ">");
		
		return result;
	}
	
	public static DictionaryEntry createDictionaryEntry(ExecutableElement getter, int version, String cutPrefix, String addPrefix) {
		DictionaryEntry result = new DictionaryEntry();
		
		result.setVersion(version);
		result.setName(getAttributeName(getter.getSimpleName().toString(), (TypeElement)getter.getEnclosingElement(), cutPrefix, addPrefix));
		result.setType(replacePrimitiveType(getter.getReturnType().toString()));
		
		return result;
	}
	
	private static Map<String, String> primitiveTypeReplacements = new HashMap<String, String>();
	
	static {
		List<String> primitiveTypes = Arrays.asList("bool", "byte", "int", "long", "char", "float", "double");
		List<Class<?>> primitiveClasses = Arrays.<Class<?>>asList(Boolean.class, Byte.class, Integer.class, Long.class, Character.class, Float.class, Double.class);
		
		for (int i = 0; i < primitiveTypes.size(); ++i){
			primitiveTypeReplacements.put("^" + primitiveTypes.get(i), primitiveClasses.get(i).getCanonicalName());
			primitiveTypeReplacements.put(primitiveTypes.get(i) + "\\[", primitiveClasses.get(i).getCanonicalName() + "\\[");
		}
	}
	
	private static String replacePrimitiveType(String type) {
		for (Map.Entry<String, String> replacement : primitiveTypeReplacements.entrySet())
			type = type.replaceFirst(replacement.getKey(), replacement.getValue());
		
		return type;
	}
}
