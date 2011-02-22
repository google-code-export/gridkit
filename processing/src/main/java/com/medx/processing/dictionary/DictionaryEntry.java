package com.medx.processing.dictionary;

import java.io.Serializable;

public class DictionaryEntry implements Serializable {
	private static final long serialVersionUID = 3701736350302492183L;
	
	private int id;
	private int version;
	private String name;
	private String type;
	private String description;
	
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

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public String toString() {
		return "DictionaryEntry [id=" + id + ", version=" + version + ", name="
				+ name + ", type=" + type + ", description=" + description
				+ "]";
	}
}
