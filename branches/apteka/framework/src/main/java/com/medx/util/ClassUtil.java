package com.medx.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassUtil {
	public static final List<String> primitiveTypes = Collections.unmodifiableList(Arrays.asList("boolean", "byte", "int", "long", "char", "float", "double"));
	
	public static final Map<String, String> primitiveArrayChars = new HashMap<String, String>();
	
	private static String ARRAY_POSTFIX = "[]";
	
	static {
		primitiveArrayChars.put("boolean", "Z");
		primitiveArrayChars.put("byte", "B");
		primitiveArrayChars.put("int", "I");
		primitiveArrayChars.put("long", "J");
		primitiveArrayChars.put("char", "C");
		primitiveArrayChars.put("float", "F");
		primitiveArrayChars.put("double", "D");
	}
	
	public static String getRawType(String type) {
		int index = type.indexOf('<');
		
		if (index != -1) {
			int lastIndex = type.lastIndexOf('>');
			type = type.substring(0, index) + type.substring(lastIndex + 1);
		}
		
		return isArray(type) ? getArrayRawType(type) : type;
	}
	
	private static boolean isArray(String type) {
		return type.endsWith(ARRAY_POSTFIX);
	}
	
	private static String getArrayRawType(String type) {
		String clazz = getArrayType(type);
		int dimension = getArrayDimension(type);
		
		if (primitiveArrayChars.containsKey(clazz))
			return TextUtil.repeat('[', dimension) + primitiveArrayChars.get(clazz);
		else
			return TextUtil.repeat('[', dimension) + "L" + clazz + ";";
	}
	
	private static int getArrayDimension(String type) {
		if (!isArray(type))
			return 0;
		else
			return 1 + getArrayDimension(type.substring(0, type.length() - ARRAY_POSTFIX.length()));
	}
	
	private static String getArrayType(String type) {
		return type.substring(0, type.indexOf('['));
	}
}
