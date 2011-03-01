package com.medx.framework.dictionary.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(namespace="http://medx.com/dictionary")
@XmlAccessorType(XmlAccessType.FIELD)
public class Dictionary implements Serializable {
	private static final long serialVersionUID = -3021789882790056395L;
	
	@XmlAttribute(required=true)
	private int version;
	
	@XmlElementWrapper
	@XmlAnyElement
	private List<TypeDescriptor> typeDescriptors;
	
	@XmlElementWrapper
	@XmlAnyElement
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
