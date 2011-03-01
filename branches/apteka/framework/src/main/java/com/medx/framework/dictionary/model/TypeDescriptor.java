package com.medx.framework.dictionary.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(namespace=Dictionary.DICTIONARY_NAMESPACE)
@XmlType(namespace=Dictionary.DICTIONARY_NAMESPACE, propOrder={})
public class TypeDescriptor extends DictionaryEntry {
	private static final long serialVersionUID = 6113820571290881884L;
	
	@XmlElement(required=true, name="class")
	private String clazz;

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
}
