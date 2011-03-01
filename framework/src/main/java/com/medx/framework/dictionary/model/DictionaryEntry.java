package com.medx.framework.dictionary.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder={})
public abstract class DictionaryEntry implements Serializable {
	private static final long serialVersionUID = -1769844964944839373L;
	
	@XmlAttribute(required=true)
	private int id;
	
	@XmlAttribute(required=false)
	private Integer version;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
}
