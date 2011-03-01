package com.medx.framework.util;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import com.medx.framework.annotation.ModelPackage;

public class DictUtil {
	private static String getAttrName(ModelPackage modelPackage, String packageName, String className, String attrName) {
		if (!packageName.isEmpty() && !className.startsWith(packageName + "."))
			throw new IllegalArgumentException("packageName | className");
		
		String resultAttrName = packageName.isEmpty() ? className : className.substring(packageName.length() + 1);
		
		resultAttrName = modelPackage.value().isEmpty() ? resultAttrName : modelPackage.value() + "." + resultAttrName;

		return resultAttrName + "." + attrName;
	}
	
	private static Package getModelPackage(Package packet) {
		if (packet.isAnnotationPresent(ModelPackage.class))
			return packet;
		else
			return getModelPackage(Package.getPackage(ClassUtil.getParentPackage(packet.getName())));
	}
	
	public static String getAttrName(Class<?> clazz, String attrName) {
		Package modelPackage = getModelPackage(clazz.getPackage());
		
		String packageName = modelPackage.getName();
		String className = clazz.getCanonicalName();
		
		return getAttrName(modelPackage.getAnnotation(ModelPackage.class), packageName, className, attrName);
	}
	
	public static String getAttrName(TypeElement clazz, PackageElement modelPackage, String attrName) {
		String packageName = modelPackage.getQualifiedName().toString();
		String className = clazz.getQualifiedName().toString();
		
		return getAttrName(modelPackage.getAnnotation(ModelPackage.class), packageName, className, attrName);
	}
	
	/*
	public static String getJavaDictionaryPackage(TypeElement clazz) {
		//DictType dictType = clazz.getAnnotation(DictType.class);
		
		String packageName = clazz.getQualifiedName().toString();
		
		packageName = packageName.contains(".") ? packageName.substring(0, packageName.lastIndexOf('.')) : "";
		
		//if (!packageName.isEmpty())
		//	packageName = packageName.substring(dictType.packageCutPrefix().length());

		return null;
		//return dictType.javaAddPrefix() + packageName;
	}
	*/
}
