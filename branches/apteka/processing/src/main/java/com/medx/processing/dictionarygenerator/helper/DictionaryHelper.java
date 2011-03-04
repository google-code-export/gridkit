package com.medx.processing.dictionarygenerator.helper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.medx.framework.annotation.ModelPackage;
import com.medx.framework.dictionary.model.AttributeDescriptor;
import com.medx.framework.dictionary.model.Dictionary;
import com.medx.framework.dictionary.model.TypeDescriptor;
import com.medx.framework.util.DictUtil;

public class DictionaryHelper {
	private final Dictionary dictionary;

	public DictionaryHelper(Dictionary dictionary) {
		this.dictionary = dictionary;
	}

	public int getNextId(int startId) {
		Integer maxId = getMaxId();
		return maxId == null ? startId : maxId + 1;
	}
	
	public Integer getMaxId() {
		Integer maxId = null;
		
		for (TypeDescriptor typeDescriptor : dictionary.getTypeDescriptors())
			if (maxId == null || typeDescriptor.getId() > maxId) {
				maxId = typeDescriptor.getId();
			}
		
		for (AttributeDescriptor attributeDescriptor : dictionary.getAttributeDescriptors())
			if (maxId == null || attributeDescriptor.getId() > maxId) {
				maxId = attributeDescriptor.getId();
			}
		
		return maxId;
	}
	
	public Set<String> getRegisteredTypes() {
		Set<String> result = new HashSet<String>();
		
		for (TypeDescriptor desc : dictionary.getTypeDescriptors())
			result.add(desc.getJavaClassName());
		
		return result;
	}
	
	public Set<String> getRegisteredAttributes(ModelPackage modelPackage, String modelPackageName, String clazz) {
		Set<String> result = new HashSet<String>();
		
		String attrNamePrefix = DictUtil.getAttrName(modelPackage, modelPackageName, clazz, "");
		
		for (AttributeDescriptor desc : dictionary.getAttributeDescriptors())
			if (desc.getName().startsWith(attrNamePrefix))
				result.add(desc.getName());
		
		return result;
	}
	
	public TypeDescriptor containsTypeDescriptor(TypeDescriptor desc) {
		for (TypeDescriptor internalDesc : dictionary.getTypeDescriptors())
			if (internalDesc.getJavaClassName().equals(desc.getJavaClassName()))
				return internalDesc;
		
		return null;
	}
	
	public AttributeDescriptor containsAttributeDescriptor(AttributeDescriptor desc) {
		for (AttributeDescriptor internalDesc : dictionary.getAttributeDescriptors())
			if (internalDesc.getName().equals(desc.getName()))
				return internalDesc;
		
		return null;
	}
	
	public static Dictionary createEmptyDictionary(int version) {
		Dictionary result = new Dictionary();
		
		result.setVersion(version);
		result.setTypeDescriptors(new ArrayList<TypeDescriptor>());
		result.setAttributeDescriptors(new ArrayList<AttributeDescriptor>());
		
		return result;
	}
}
