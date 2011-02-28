package com.medx.util;

import com.medx.type.annotation.DictType;

public class DictUtil {
	public static String getAttrName(Class<?> clazz, String name) {
		DictType dictType = clazz.getAnnotation(DictType.class);
		
		String attrName = clazz.getCanonicalName();
		
		if (!dictType.packageCutPrefix().isEmpty())
			attrName = attrName.substring(dictType.packageCutPrefix().length() + 1);
		
		if (!dictType.xmlAddPrefix().isEmpty())
			attrName = dictType.xmlAddPrefix() + "." + attrName;
		
		return attrName + "." + name;
	}
	
	public static String getJavaDictionary(Class<?> clazz) {
		return null;
	}
}
