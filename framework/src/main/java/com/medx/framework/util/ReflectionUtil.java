package com.medx.framework.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.medx.framework.annotation.ModelPackage;

public class ReflectionUtil {
	public static final String GETTER_PATTERN  = "get[A-Z].*";
	
	private static final Map<String, Class<?>> primitiveMap = new HashMap<String, Class<?>>();
	
	static {
		primitiveMap.put("boolean", boolean.class);
		primitiveMap.put("byte", byte.class);
		primitiveMap.put("char", char.class);
		primitiveMap.put("short", short.class);
		primitiveMap.put("int", int.class);
		primitiveMap.put("long", long.class);
		primitiveMap.put("float", float.class);
		primitiveMap.put("double", double.class);
	}
	
	public static Class<?> getPrimitiveClass(String className) {
		return primitiveMap.get(className);
	}
	
	public static List<Method> getGetters(Class<?> clazz) {
		List<Method> result = new ArrayList<Method>();
		
		for(Method method : clazz.getDeclaredMethods())
			if (isGetter(method))
				result.add(method);
				
		return result;
	}
	
	public static boolean isGetter(Method method) {
		if (method.getParameterTypes().length > 0)
			return false;

		if (method.getReturnType() == Void.TYPE)
			return false;
		
		if (!method.getName().matches(ReflectionUtil.GETTER_PATTERN))
			return false;
		
		return true;
	}
	
	public static Package getModelPackage(Package packet) {
		if (packet.isAnnotationPresent(ModelPackage.class))
			return packet;
		else if (!ClassUtil.hasParentPackage(packet.getName()))
			throw new IllegalArgumentException("packet");
		else {
			boolean hasPackageInfo = false;
			
			String parentPackage = packet.getName();
			
			do {
				parentPackage = ClassUtil.getParentPackage(parentPackage);
				
				try {
					Class.forName(parentPackage + ".package-info");
				} catch (ClassNotFoundException e) {
					continue;
				}
				
				hasPackageInfo = true;
			} while (ClassUtil.hasParentPackage(parentPackage) && !hasPackageInfo);
			
			if (hasPackageInfo)
				return getModelPackage(Package.getPackage(parentPackage));
			else
				throw new IllegalArgumentException("packet");
		}
	}
}
