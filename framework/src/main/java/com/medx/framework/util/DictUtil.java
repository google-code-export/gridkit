package com.medx.framework.util;

import com.medx.framework.annotation.JavaDictionary;
import com.medx.framework.annotation.ModelPackage;

public class DictUtil {
	public static String getAttrName(ModelPackage modelPackage, String modelPackageName, String className, String attrName) {
		if (!ClassUtil.isClassInPackage(className, modelPackageName))
			throw new IllegalArgumentException("modelPackageName | className");
		
		String result = modelPackageName.isEmpty() ? className : className.substring(modelPackageName.length() + 1);
		
		result = modelPackage.value().isEmpty() ? result : modelPackage.value() + "." + result;

		return result + "." + attrName;
	}
	
	public static String getJavaDictionaryPackage(String modelClassPackage, String modelPackage, JavaDictionary javaDictionary) {
		if (!ClassUtil.isPackageInPackage(modelClassPackage, modelPackage))
			throw new IllegalArgumentException("modelClassPackage | modelPackage");
		
		String resultPackage = "";
		
		if (!modelPackage.equals(modelClassPackage))
			resultPackage = modelPackage.isEmpty() ? modelClassPackage : modelClassPackage.substring(modelPackage.length() + 1);
		
		if (resultPackage.isEmpty())
			return javaDictionary.value();
		else if (javaDictionary.value().isEmpty())
			return resultPackage;
		else
			return javaDictionary.value() + "." + resultPackage;
	}
}
