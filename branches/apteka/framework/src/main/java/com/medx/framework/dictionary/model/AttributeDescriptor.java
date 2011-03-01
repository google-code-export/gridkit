package com.medx.framework.dictionary.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace=Dictionary.DICTIONARY_NAMESPACE, propOrder={})
public class AttributeDescriptor extends DictionaryEntry {
	private static final long serialVersionUID = 2835020522282007694L;

	@XmlElement(required=true)
	private String name;
	
	@XmlElement(required=true, name="class")
	private String clazz;
	
	@XmlElement(required=false, defaultValue="")
	private String description;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
}
