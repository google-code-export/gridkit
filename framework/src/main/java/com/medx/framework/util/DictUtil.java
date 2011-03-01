package com.medx.framework.util;

import javax.lang.model.element.TypeElement;

import com.medx.framework.type.annotation.DictType;

//TODO fix for classes without default package
public class DictUtil {
	public static String getAttrName(DictType dictType, String className, String attrName) {
		if (!dictType.packageCutPrefix().isEmpty())
			className = className.substring(dictType.packageCutPrefix().length() + 1);
		
		if (!dictType.xmlAddPrefix().isEmpty())
			className = dictType.xmlAddPrefix() + "." + className;
		
		return className + "." + attrName;
	}
	
	public static String getAttrName(Class<?> clazz, String attrName) {
		return getAttrName(clazz.getAnnotation(DictType.class), clazz.getCanonicalName(), attrName);
	}
	
	public static String getAttrName(TypeElement clazz, String attrName) {
		return getAttrName(clazz.getAnnotation(DictType.class), clazz.getQualifiedName().toString(), attrName);
	}
	
	public static String getJavaDictionaryPackage(TypeElement clazz) {
		DictType dictType = clazz.getAnnotation(DictType.class);
		
		String packageName = clazz.getQualifiedName().toString();
		
		packageName = packageName.contains(".") ? packageName.substring(0, packageName.lastIndexOf('.')) : "";
		
		if (!packageName.isEmpty())
			packageName = packageName.substring(dictType.packageCutPrefix().length());

		return dictType.javaAddPrefix() + packageName;
	}
}
