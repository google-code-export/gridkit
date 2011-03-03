package com.medx.framework.dictionary.model;

public class ObjectFactory {

	public Dictionary createDictionary() {
		return new Dictionary();
	}
	
	public AttributeDescriptor createAttributeDescriptor() {
		return new AttributeDescriptor();
	}
	
	public TypeDescriptor createTypeDescriptor() {
		return new TypeDescriptor();
	}
}
