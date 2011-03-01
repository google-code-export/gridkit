package com.medx.framework.dictionary.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.medx.framework.dictionary.model.adapter.IntegerAdapter;

@XmlType(namespace=Dictionary.DICTIONARY_NAMESPACE, propOrder={})
@XmlAccessorType(XmlAccessType.FIELD)
public class DictionaryEntry implements Serializable {
	private static final long serialVersionUID = -1769844964944839373L;
	
	@XmlID
	@XmlJavaTypeAdapter(IntegerAdapter.class)
	@XmlAttribute(required=true)
	private Integer id;
	
	@XmlAttribute(required=false)
	private int version;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
}
