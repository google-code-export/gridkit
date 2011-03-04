package com.medx.framework.dictionary.model;

import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
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
