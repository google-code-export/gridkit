package com.medx.framework.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.medx.framework.annotation.ModelPackage;

public class ReflectionUtil {
	public static final String GETTER_PATTERN  = "get[A-Z].*";
	
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
