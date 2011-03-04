package com.medx.processing.dictionarygenerator.helper;

import java.util.ArrayList;

import com.medx.framework.dictionary.model.AttributeDescriptor;
import com.medx.framework.dictionary.model.Dictionary;
import com.medx.framework.dictionary.model.TypeDescriptor;

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
	
	public TypeDescriptor addTypeDescriptor(TypeDescriptor otherDesc) {
		for (TypeDescriptor desc : dictionary.getTypeDescriptors())
			if (desc.getJavaClassName().equals(otherDesc.getJavaClassName()))
				return desc;
		
		dictionary.getTypeDescriptors().add(otherDesc);
		
		return otherDesc;
	}
	
	public AttributeDescriptor addAttributeDescriptor(AttributeDescriptor otherDesc) {
		for (AttributeDescriptor desc : dictionary.getAttributeDescriptors())
			if (desc.getName().equals(otherDesc.getName()))
				return desc;
		
		dictionary.getAttributeDescriptors().add(otherDesc);
		
		return otherDesc;
	}
	
	public Dictionary getDictionary() {
		return dictionary;
	}
	
	public static Dictionary createEmptyDictionary(int version) {
		Dictionary result = new Dictionary();
		
		result.setVersion(version);
		result.setTypeDescriptors(new ArrayList<TypeDescriptor>());
		result.setAttributeDescriptors(new ArrayList<AttributeDescriptor>());
		
		return result;
	}
}
