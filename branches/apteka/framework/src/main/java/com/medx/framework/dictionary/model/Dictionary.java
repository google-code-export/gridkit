package com.medx.framework.dictionary.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(propOrder={})
public class Dictionary implements Serializable {
	private static final long serialVersionUID = -3021789882790056395L;
	
	public static final String FRAMEWORK_DICTIONARY_NAMESPACE = "http://medx.com/framework/dictionary";
	
	@XmlAttribute(required=true)
	private int version;
	
	@XmlElementWrapper
	@XmlElement(name="typeDescriptor")
	private List<TypeDescriptor> typeDescriptors;
	
	@XmlElementWrapper
	@XmlElement(name="attributeDescriptor")
	private List<AttributeDescriptor> attributeDescriptors;

	public List<TypeDescriptor> getTypeDescriptors() {
		return typeDescriptors;
	}

	public void setTypeDescriptors(List<TypeDescriptor> typeDescriptors) {
		this.typeDescriptors = typeDescriptors;
	}

	public List<AttributeDescriptor> getAttributeDescriptors() {
		return attributeDescriptors;
	}

	public void setAttributeDescriptors(List<AttributeDescriptor> attributeDescriptors) {
		this.attributeDescriptors = attributeDescriptors;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
}
