package com.medx.framework.dictionary.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;

@XmlAccessorType(XmlAccessType.FIELD)
public class DictionaryEntry implements Serializable {
	private static final long serialVersionUID = -1769844964944839373L;
	
	@XmlID
	@XmlAttribute(required=true)
	private String id;
	
	@XmlAttribute(required=false)
	private int version;
	
	public String getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = String.valueOf(id);
	}
	
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
}
