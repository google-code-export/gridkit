package com.medx.framework.dictionary;

import com.medx.framework.dictionary.model.AttributeDescriptor;
import com.medx.framework.dictionary.model.Dictionary;
import com.medx.framework.dictionary.model.TypeDescriptor;

public class DictionaryManager {
	private final Dictionary dictionary;

	public DictionaryManager(Dictionary dictionary) {
		this.dictionary = dictionary;
	}

	public int getMaximumId(int startId) {
		int maxId = startId;
		
		for (TypeDescriptor typeDescriptor : dictionary.getTypeDescriptors())
			if (typeDescriptor.getId() > maxId)
				maxId = typeDescriptor.getId();
		
		for (AttributeDescriptor attributeDescriptor : dictionary.getAttributeDescriptors())
			if (attributeDescriptor.getId() > maxId)
				maxId = attributeDescriptor.getId();
		
		return maxId;
	}
	
	public Dictionary getDictionary() {
		return dictionary;
	}
}
