package com.medx.framework.util;

import com.medx.framework.annotation.JavaDictionary;
import com.medx.framework.annotation.ModelPackage;

public class DictUtil {
	public static String getAttrName(ModelPackage modelPackage, String modelPackageName, String className, String attrName) {
		if (!ClassUtil.isClassInPackage(className, modelPackageName))
			throw new IllegalArgumentException("modelPackageName | className");
		
		String resultAttrName = modelPackageName.isEmpty() ? className : className.substring(modelPackageName.length() + 1);
		
		resultAttrName = modelPackage.value().isEmpty() ? resultAttrName : modelPackage.value() + "." + resultAttrName;

		return resultAttrName + "." + attrName;
	}
	
	private static Package getModelPackage(Package packet) {
		if (packet.isAnnotationPresent(ModelPackage.class))
			return packet;
		else if (!ClassUtil.hasParentPackage(packet.getName()))
			throw new IllegalArgumentException("packet");
		else
			return getModelPackage(Package.getPackage(ClassUtil.getParentPackage(packet.getName())));
	}
	
	public static String getAttrName(Class<?> clazz, String attrName) {
		Package modelPackage = getModelPackage(clazz.getPackage());
		
		String packageName = modelPackage.getName();
		String className = clazz.getCanonicalName();
		
		return getAttrName(modelPackage.getAnnotation(ModelPackage.class), packageName, className, attrName);
	}
	
	public static String getJavaDictionaryPackage(String modelClassPackage, String modelPackage, JavaDictionary javaDictionary) {
		if (!ClassUtil.isPackageInPackage(modelClassPackage, modelPackage))
			throw new IllegalArgumentException("modelClassPackage | modelPackage");
		
		String resultPackage = "";
		
		if (!modelPackage.equals(modelClassPackage))
			resultPackage = modelPackage.isEmpty() ? modelClassPackage : modelClassPackage.substring(modelPackage.length() + 1);
		
		resultPackage = javaDictionary.value().isEmpty() ? resultPackage : javaDictionary.value() + "." + resultPackage;
		
		return resultPackage;
	}
}
