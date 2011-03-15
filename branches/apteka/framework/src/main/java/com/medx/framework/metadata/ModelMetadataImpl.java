package com.medx.framework.metadata;

import static com.medx.framework.metadata.ClassKeyFactory.createArrayClassKey;
import static com.medx.framework.metadata.ClassKeyFactory.createCollectionClassKey;
import static com.medx.framework.metadata.ClassKeyFactory.createEntryClassKey;
import static com.medx.framework.metadata.ClassKeyFactory.createEnumClassKey;
import static com.medx.framework.metadata.ClassKeyFactory.createListClassKey;
import static com.medx.framework.metadata.ClassKeyFactory.createMapClassKey;
import static com.medx.framework.metadata.ClassKeyFactory.createSetClassKey;
import static com.medx.framework.metadata.ClassKeyFactory.createUserClassKey;
import static com.medx.framework.metadata.ClassKeyFactory.getPrimitiveMap;
import static com.medx.framework.metadata.ClassKeyFactory.getStandardMap;
import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.medx.framework.annotation.ModelPackage;
import com.medx.framework.dictionary.model.AttributeDescriptor;
import com.medx.framework.dictionary.model.Dictionary;
import com.medx.framework.dictionary.model.TypeDescriptor;
import com.medx.framework.util.DictUtil;
import com.medx.framework.util.ReflectionUtil;
import com.medx.framework.util.TextUtil;

public class ModelMetadataImpl implements ModelMetadata {
	private static final Logger log = LoggerFactory.getLogger(ModelMetadataImpl.class);
	
	private static final Pattern mapPattern = Pattern.compile("^java\\.util\\.Map<(.*)\\,(.*)>$");
	private static final Pattern setPattern = Pattern.compile("^java\\.util\\.Set<(.*)>$");
	private static final Pattern listPattern = Pattern.compile("^java\\.util\\.List<(.*)>$");
	private static final Pattern arrayPattern = Pattern.compile("^(.*)\\[\\]$");
	private static final Pattern collectionPattern = Pattern.compile("^java\\.util\\.Collection<(.*)>$");
	
	private ConcurrentMap<Integer, TypedAttrKey> attrKeyById = new ConcurrentHashMap<Integer, TypedAttrKey>();
	private ConcurrentMap<String, TypedAttrKey> attrKeyByName = new ConcurrentHashMap<String, TypedAttrKey>();

	private ConcurrentMap<Integer, ClassKey> typeKeyById = new ConcurrentHashMap<Integer, ClassKey>();
	private ConcurrentMap<Class<?>, ClassKey> typeKeyByClass = new ConcurrentHashMap<Class<?>, ClassKey>();
	
	private ConcurrentMap<Class<?>, List<TypedAttrKey>> attrKeyByClass = new ConcurrentHashMap<Class<?>, List<TypedAttrKey>>();
	
	public ModelMetadataImpl(Dictionary... dictionaries) {
		for (Dictionary dictionary : dictionaries)
			loadDictionary(dictionary);
	}
	
	public synchronized void loadDictionary(Dictionary dictionary) {
		loadTypeDictionary(dictionary);
		loadAttrDictionary(dictionary);
		
		for (Class<?> javaClass : typeKeyByClass.keySet())
			if (!attrKeyByClass.containsKey(javaClass))
				attrKeyByClass.put(javaClass, getAttrKeysInternal(javaClass));
	}
	
	private void loadTypeDictionary(Dictionary dictionary) {
		for (TypeDescriptor typeDescriptor : dictionary.getTypeDescriptors()) {
			if (typeKeyById.containsKey(typeDescriptor.getId())) {
				log.warn(format("Type with id '%d' already presented", typeDescriptor.getId()));
				continue;
			}
			
			Class<?> clazz = null;
			try {
				clazz = Class.forName(typeDescriptor.getJavaClassName());
			} catch (ClassNotFoundException e) {
				log.warn(format("Failded to find class '%s'", typeDescriptor.getJavaClassName()));
				continue;
			}
			
			if (typeKeyByClass.containsKey(clazz)) {
				log.warn(format("Type with class '%s' already presented", typeDescriptor.getJavaClassName()));
				continue;
			}
			
			ClassKey typeKey = createUserClassKey(typeDescriptor.getId(), typeDescriptor.getVersion(), clazz);
			
			typeKeyById.put(typeDescriptor.getId(), typeKey);
			typeKeyByClass.put(clazz, typeKey);
		}
	}
	
	private void loadAttrDictionary(Dictionary dictionary) {
		for (AttributeDescriptor attributeDescriptor : dictionary.getAttributeDescriptors()) {
			if (attrKeyById.containsKey(attributeDescriptor.getId())) {
				log.warn(format("AttrKey with id '%d' already presented", attributeDescriptor.getId()));
				continue;
			}
			
			if (attrKeyByName.containsKey(attributeDescriptor.getName())) {
				log.warn(format("AttrKey with name '%s' already presented", attributeDescriptor.getName()));
				continue;
			}
			
			String className = attributeDescriptor.getClazz();
			
			ClassKey classKey = createClassKey(className);
			
			if (classKey == null) {
				log.warn(format("Failed to create ClassKey for class '%s'", className));
				continue;
			}
			
			TypedAttrKey attrKey = new TypedAttrKey(attributeDescriptor.getId(), attributeDescriptor.getName(),
				attributeDescriptor.getVersion(), attributeDescriptor.getDescription(), classKey);
			
			attrKeyById.put(attributeDescriptor.getId(), attrKey);
			attrKeyByName.put(attributeDescriptor.getName(), attrKey);
		}
	}

	@Override
	public List<TypedAttrKey> getAttrKeys(Class<?> javaClass) {
		return Collections.unmodifiableList(attrKeyByClass.get(javaClass));
	}
	
	private List<TypedAttrKey> getAttrKeysInternal(Class<?> clazz) {
		List<TypedAttrKey> result = new ArrayList<TypedAttrKey>();
		
		Package modelPacket = ReflectionUtil.getModelPackage(clazz.getPackage());
		
		String modelPackageName = modelPacket.getName();
		ModelPackage modelPackage = modelPacket.getAnnotation(ModelPackage.class);
		
		for (Method getter : ReflectionUtil.getGetters(clazz)) {
			String getterName = TextUtil.getCamelPostfix(getter.getName());
			
			String modelAttrName = DictUtil.getAttrName(modelPackage, modelPackageName, clazz.getCanonicalName(), getterName);
			
			result.add(this.getAttrKey(modelAttrName));
		}
		
		return result;
	}
	
	@Override
	public Set<Integer> getTypeIds(Set<Integer> candidates) {
		HashSet<Integer> result = new HashSet<Integer>();
		
		for (Integer typeId : candidates)
			if (typeKeyById.containsKey(typeId))
				result.add(typeId);
		
		return result;
	}
	
	private ClassKey createClassKey(String className) {
		Matcher matcher = mapPattern.matcher(className);
		
		if (matcher.find()) {
			ClassKey keyClassKey = createClassKey(matcher.group(1));
			ClassKey valueClassKey = createClassKey(matcher.group(2));
			
			if (keyClassKey != null && valueClassKey != null && !keyClassKey.getType().isPrimitiveOrEntry() && !valueClassKey.getType().isPrimitiveOrEntry())
				return createMapClassKey(createEntryClassKey(keyClassKey, valueClassKey));
			else
				return null;
		}
		
		matcher = setPattern.matcher(className);
		
		if (matcher.find()) {
			ClassKey classKey = createClassKey(matcher.group(1));
			
			if (classKey != null && !classKey.getType().isPrimitiveOrEntry())
				return createSetClassKey(classKey);
			else
				return null;
		}
		
		matcher = listPattern.matcher(className);
		
		if (matcher.find()) {
			ClassKey classKey = createClassKey(matcher.group(1));
			
			if (classKey != null && !classKey.getType().isPrimitiveOrEntry())
				return createListClassKey(classKey);
			else
				return null;
		}
		
		matcher = collectionPattern.matcher(className);
		
		if (matcher.find()) {
			ClassKey classKey = createClassKey(matcher.group(1));
			
			if (classKey != null && !classKey.getType().isPrimitiveOrEntry())
				return createCollectionClassKey(classKey);
			else
				return null;
		}
		
		matcher = arrayPattern.matcher(className);
		
		if (matcher.find()) {
			ClassKey classKey = createClassKey(matcher.group(1));
			
			if (classKey != null && !classKey.getType().isEntry())
				return createArrayClassKey(classKey);
			else
				return null;
		}
		
		Class<?> javaClass = null;
		
		try {
			javaClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			javaClass = ReflectionUtil.getPrimitiveClass(className);
			
			if (javaClass == null)
				return null;
		}
		
		if (javaClass.isEnum())
			return createEnumClassKey(javaClass);
		
		if (getStandardMap().containsKey(javaClass))
			return getStandardMap().get(javaClass);
		
		if (getPrimitiveMap().containsKey(javaClass))
			return getPrimitiveMap().get(javaClass);
		
		if (typeKeyByClass.containsKey(javaClass))
			return typeKeyByClass.get(javaClass);
		
		return null;
	}
	
	@Override
	public TypedAttrKey getAttrKey(int id) {
		return attrKeyById.get(id);
	}

	@Override
	public TypedAttrKey getAttrKey(String name) {
		return attrKeyByName.get(name);
	}
	
	@Override
	public ClassKey getClassKey(int id) {
		return typeKeyById.get(id);
	}

	@Override
	public ClassKey getClassKey(Class<?> clazz) {
		return typeKeyByClass.get(clazz);
	}
}
