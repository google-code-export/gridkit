package com.medx.framework.metadata;

import static java.lang.String.format;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.medx.framework.dictionary.model.AttributeDescriptor;
import com.medx.framework.dictionary.model.Dictionary;
import com.medx.framework.dictionary.model.TypeDescriptor;
import com.medx.framework.util.ClassUtil;

public class ModelMetadataImpl implements ModelMetadata {
	private static final Logger log = LoggerFactory.getLogger(ModelMetadataImpl.class);
	
	private ConcurrentMap<Integer, AttrKey<?>> attrKeyById = new ConcurrentHashMap<Integer, AttrKey<?>>();
	private ConcurrentMap<String, AttrKey<?>> attrKeyByName = new ConcurrentHashMap<String, AttrKey<?>>();

	private ConcurrentMap<Integer, TypeKey<?>> typeKeyById = new ConcurrentHashMap<Integer, TypeKey<?>>();
	private ConcurrentMap<Class<?>, TypeKey<?>> typeKeyByClass = new ConcurrentHashMap<Class<?>, TypeKey<?>>();
	
	public ModelMetadataImpl(Dictionary... dictionaries) {
		for (Dictionary dictionary : dictionaries)
			loadDictionary(dictionary);
	}
	
	public synchronized void loadDictionary(Dictionary dictionary) {
		loadTypeDictionary(dictionary);
		loadAttrDictionary(dictionary);
	}
	
	private synchronized void loadAttrDictionary(Dictionary dictionary) {
		for (AttributeDescriptor attributeDescriptor : dictionary.getAttributeDescriptors()) {
			if (attrKeyById.containsKey(attributeDescriptor.getId())) {
				log.warn(format("AttrKey with id '%d' already presented", attributeDescriptor.getId()));
				continue;
			}
			
			if (attrKeyByName.containsKey(attributeDescriptor.getName())) {
				log.warn(format("AttrKey with name '%s' already presented", attributeDescriptor.getName()));
				continue;
			}
			
			String className = ClassUtil.getRawClass(attributeDescriptor.getClazz());
			
			Class<?> clazz = null;
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				log.warn(format("Failded to find class '%s'", className));
				continue;
			}
			
			AttrKey<?> attrKey = new AttrKey<Object>(attributeDescriptor.getId(), attributeDescriptor.getName(),
				attributeDescriptor.getVersion(), clazz, attributeDescriptor.getDescription());
			
			attrKeyById.put(attributeDescriptor.getId(), attrKey);
			attrKeyByName.put(attributeDescriptor.getName(), attrKey);
		}
	}
	
	private synchronized void loadTypeDictionary(Dictionary dictionary) {
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
			
			TypeKey<?> typeKey = new TypeKey<Object>(typeDescriptor.getId(), typeDescriptor.getVersion(), clazz);
			
			typeKeyById.put(typeDescriptor.getId(), typeKey);
			typeKeyByClass.put(clazz, typeKey);
		}
	}

	@Override
	public Set<Integer> getTypeIds(Set<Integer> candidates) {
		HashSet<Integer> result = new HashSet<Integer>();
		
		for (Integer typeId : candidates)
			if (typeKeyById.containsKey(typeId))
				result.add(typeId);
		
		return result;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> AttrKey<T> getAttrKey(int id) {
		return (AttrKey<T>)attrKeyById.get(id);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> AttrKey<T> getAttrKey(String name) {
		return (AttrKey<T>)attrKeyByName.get(name);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> TypeKey<T> getTypeKey(int id) {
		return (TypeKey<T>)typeKeyById.get(id);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> TypeKey<T> getTypeKey(Class<T> clazz) {
		return (TypeKey<T>)typeKeyByClass.get(clazz);
	}
}
