package com.medx.framework.attribute;

import static java.lang.String.format;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.medx.framework.dictionary.model.AttributeDescriptor;
import com.medx.framework.dictionary.model.Dictionary;
import com.medx.framework.util.ClassUtil;

public class AttrKeyRegistryImpl implements AttrKeyRegistry {
	private static final Logger log = LoggerFactory.getLogger(AttrKeyRegistryImpl.class);
	
	private ConcurrentMap<Integer, AttrKey<?>> attrKeyById = new ConcurrentHashMap<Integer, AttrKey<?>>();
	private ConcurrentMap<String, AttrKey<?>> attrKeyByName = new ConcurrentHashMap<String, AttrKey<?>>();

	public AttrKeyRegistryImpl(Dictionary... dictionaries) {
		for (Dictionary dictionary : dictionaries)
			loadDictionary(dictionary);
	}
	
	public synchronized void loadDictionary(Dictionary dictionary) {
		for (AttributeDescriptor attributeDescriptor : dictionary.getAttributeDescriptors()) {
			if (attrKeyById.containsKey(attributeDescriptor.getId())) {
				log.warn(format("AttrKey with id '%d' already presented", attributeDescriptor.getId()));
				continue;
			}
			
			if (attrKeyByName.containsKey(attributeDescriptor.getName())) {
				log.warn(format("AttrKey with name '%s' already presented", attributeDescriptor.getName()));
				continue;
			}
			
			String className = ClassUtil.getRawType(attributeDescriptor.getClazz());
			
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
}
