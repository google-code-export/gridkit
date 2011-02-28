package com.medx.dictionary;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Dictionary implements Serializable {
	private static final long serialVersionUID = -3021789882790056395L;
	
	@XmlAttribute(required=true)
	private int version;
	
	@XmlAnyElement
	private List<DictionaryEntry> entries;

	public List<DictionaryEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<DictionaryEntry> entries) {
		this.entries = entries;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
}
