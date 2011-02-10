package org.apteka.insurance.dictionary.generator;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

import org.apache.commons.beanutils.PropertyUtils;
import org.apteka.insurance.attribute.annotation.AttrToDict;

public class DictionaryEntryUtil {
	private static final Map<Class<?>, Class<?>> classTransform = new HashMap<Class<?>, Class<?>>();
	
	public static List<DictionaryEntry> describe(Class<?> clazz, int startId, String cutPrefix) {
		if (!clazz.getCanonicalName().startsWith(cutPrefix + "."))
			throw new IllegalArgumentException(format("Canonical name for '%s' must starts with %s", clazz.getCanonicalName(), cutPrefix));
		
		String namePerefix = clazz.getCanonicalName().substring(cutPrefix.length() + 1);
		
		List<DictionaryEntry> result = new ArrayList<DictionaryEntry>();
		
		for (PropertyDescriptor propDesc : PropertyUtils.getPropertyDescriptors(clazz)) {
			DictionaryEntry entry = describe(propDesc, namePerefix);
			
			if (entry != null) {
				entry.setId(startId++);
				result.add(entry);
			}
		}
		
		return result;
	}
	
	private static DictionaryEntry describe(PropertyDescriptor propDesc, String namePerefix) {
		Method readMethod = propDesc.getReadMethod();
		
		if (readMethod == null || readMethod.getAnnotation(AttrToDict.class) == null)
			return null;
		
		AttrToDict attrToDict = readMethod.getAnnotation(AttrToDict.class);
		
		DictionaryEntry result = new DictionaryEntry();
		
		result.setName(namePerefix + "." + propDesc.getDisplayName());
		result.setType(wrapIfPrimitive(propDesc.getPropertyType()));
		result.setDescription(attrToDict.value());
		
		return result;
	}
	
	private static Class<?> wrapIfPrimitive(Class<?> clazz) {
		return clazz.isPrimitive() ? classTransform.get(clazz) : clazz;
	}
	
	public static String toXML(DictionaryEntry entry) {
		String result = format("\t<attribute id=\"%d\">\n", entry.getId());
		result += format("\t\t<name>%s</name>\n", entry.getName());
		result += format("\t\t<type>%s</type>\n", getPrinatbleClassName(entry.getType()));
		
		if (!"".equals(entry.getDescription()))
			result += format("\t\t<description>%s</description>\n", entry.getDescription());
		
		result += "\t</attribute>\n";
		
		return result;
	}
	
	private static String getPrinatbleClassName(Class<?> clazz) {
		String javaLangPrefix = "java.lang.";
		
		return clazz.getCanonicalName().startsWith(javaLangPrefix) ? 
			   clazz.getCanonicalName().substring(javaLangPrefix.length()) : 
			   clazz.getCanonicalName();
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
