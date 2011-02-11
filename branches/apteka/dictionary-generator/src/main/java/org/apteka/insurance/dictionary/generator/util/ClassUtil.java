package org.apteka.insurance.dictionary.generator.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

import org.apache.commons.beanutils.PropertyUtils;
import org.apteka.insurance.attribute.annotation.AttrToDict;
import org.apteka.insurance.dictionary.generator.DictionaryEntry;

public class ClassUtil {
	private static final Map<Class<?>, Class<?>> classTransform = new HashMap<Class<?>, Class<?>>();
	
	public static List<DictionaryEntry> describe(Class<?> clazz, String cutPrefix) {
		String namePerefix = getAttributeNamePerefix(clazz, cutPrefix);
		
		List<DictionaryEntry> result = new ArrayList<DictionaryEntry>();
		
		for (PropertyDescriptor propDesc : PropertyUtils.getPropertyDescriptors(clazz))
			if (isDescribable(propDesc))
				result.add(describe(propDesc, namePerefix));
		
		return result;
	}
	
	private static String getAttributeNamePerefix(Class<?> clazz, String cutPrefix) {
		if (!GeneralUtil.hasText(cutPrefix))
			return clazz.getCanonicalName();
		
		if (!clazz.getCanonicalName().startsWith(cutPrefix + "."))
			throw new IllegalArgumentException(format("Canonical name for '%s' must starts with %s", clazz.getCanonicalName(), cutPrefix));
		
		return clazz.getCanonicalName().substring(cutPrefix.length() + 1);

	}
	
	private static DictionaryEntry describe(PropertyDescriptor propDesc, String namePerefix) {
		Method readMethod = propDesc.getReadMethod();
		
		DictionaryEntry result = new DictionaryEntry();
		
		result.setName(namePerefix + "." + propDesc.getDisplayName());
		result.setType(getMethodReturnType(readMethod).replaceAll("java\\.lang\\.", ""));
		result.setDescription(readMethod.getAnnotation(AttrToDict.class).value());
		
		return result;
	}
	
	private static boolean isDescribable(PropertyDescriptor propDesc) {
		Method readMethod = propDesc.getReadMethod();
		
		return readMethod != null && readMethod.getAnnotation(AttrToDict.class) != null;
	}
	
	private static String getMethodReturnType(Method method) {
		Type returnType = method.getGenericReturnType();
		
		if (returnType instanceof Class<?>)
			return wrapIfPrimitive((Class<?>)returnType).getCanonicalName();
		else
			return returnType.toString();
	}
	
	private static Class<?> wrapIfPrimitive(Class<?> clazz) {
		return clazz.isPrimitive() ? classTransform.get(clazz) : clazz;
	}
	
	static {
		classTransform.put(boolean.class, Boolean.class);
		classTransform.put(char.class, Character.class);
		classTransform.put(byte.class, Byte.class);
		classTransform.put(short.class, Short.class);
		classTransform.put(int.class, Integer.class);
		classTransform.put(long.class, Long.class);
		classTransform.put(float.class, Float.class);
		classTransform.put(double.class, Double.class);
	}
}
