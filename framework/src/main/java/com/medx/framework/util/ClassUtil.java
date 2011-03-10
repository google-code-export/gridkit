package com.medx.framework.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassUtil {
	public static final List<String> primitiveTypes = Collections.unmodifiableList(Arrays.asList("boolean", "byte", "char", "short", "int", "long", "float", "double"));
	
	private static String ARRAY_POSTFIX = "[]";
	
	private static final Map<String, String> primitiveArrayChars = new HashMap<String, String>();
	
	static {
		primitiveArrayChars.put("boolean", "Z");
		primitiveArrayChars.put("byte", "B");
		primitiveArrayChars.put("char", "C");
		primitiveArrayChars.put("short", "S");
		primitiveArrayChars.put("int", "I");
		primitiveArrayChars.put("long", "J");
		primitiveArrayChars.put("float", "F");
		primitiveArrayChars.put("double", "D");
	}
	
	private static Map<String, Class<?>> primitiveTypeMapping = new HashMap<String, Class<?>>();
	
	static {
		primitiveTypeMapping.put("bool", Boolean.class);
		primitiveTypeMapping.put("byte", Byte.class);
		primitiveTypeMapping.put("char", Character.class);
		primitiveTypeMapping.put("short", Short.class);
		primitiveTypeMapping.put("int", Integer.class);
		primitiveTypeMapping.put("long", Long.class);
		primitiveTypeMapping.put("float", Float.class);
		primitiveTypeMapping.put("double", Double.class);
	}
	
	public static String getSimpleClassName(String clazz) {
		return clazz.contains(".") ? clazz.substring(clazz.lastIndexOf('.') + 1) : clazz;
	}
	
	public static String getClassPackage(String clazz) {
		return clazz.contains(".") ? clazz.substring(0, clazz.lastIndexOf('.')) : "";
	}
	
	public static String getFullClassName(String clazz, String packet) {
		return packet.isEmpty() ? clazz : packet + "." + clazz;
	}
	
	public static boolean isClassInPackage(String clazz, String packet) {
		return packet.isEmpty() ? true : clazz.startsWith(packet + ".");
	}
	
	public static boolean isPackageInPackage(String childPackage, String parentPackage) {
		if (parentPackage.equals(childPackage))
			return true;
		
		return parentPackage.isEmpty() ? true : childPackage.startsWith(parentPackage + ".");
	}
	
	public static boolean hasParentPackage(String packageName) {
		return !packageName.isEmpty();
	}
	
	public static String getParentPackage(String packageName) {
		if (packageName.contains("."))
			return packageName.substring(0, packageName.lastIndexOf('.'));
		else if (!hasParentPackage(packageName))
			throw new IllegalArgumentException("packageName");
		else
			return "";
	}
	
	public static String replacePrimitiveType(String clazz) {
		if (primitiveTypeMapping.keySet().contains(clazz))
			return primitiveTypeMapping.get(clazz).getCanonicalName();
		return clazz;
	}
	
	public static String getCanonicalClass(String clazz) {
		int index = clazz.indexOf('<');
		
		if (index != -1) {
			int lastIndex = clazz.lastIndexOf('>');
			clazz = clazz.substring(0, index) + clazz.substring(lastIndex + 1);
		}
		
		return clazz;
	}
	
	public static String getRawClass(String clazz) {
		clazz = getCanonicalClass(clazz);
		
		return isArray(clazz) ? getArrayRawClass(clazz) : clazz;
	}
	
	public static Class<?> getRawClassInstance(String clazz) throws ClassNotFoundException {
		if (primitiveTypeMapping.containsKey(clazz))
			return primitiveTypeMapping.get(clazz);
		else
			return Class.forName(getRawClass(clazz));
	}
	
	private static boolean isArray(String type) {
		return type.endsWith(ARRAY_POSTFIX);
	}
	
	private static String getArrayRawClass(String type) {
		String clazz = getArrayElementClass(type);
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
	
	private static String getArrayElementClass(String type) {
		return type.substring(0, type.indexOf('['));
	}
}
