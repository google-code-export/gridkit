package org.apteka.insurance.dictionary.generator;

import java.io.Serializable;

public class DictionaryEntry implements Serializable {
	private static final long serialVersionUID = 3701736350302492183L;
	
	private int id;
	private String name;
	private Class<?> type;
	private String description;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Class<?> getType() {
		return type;
	}
	
	public void setType(Class<?> type) {
		this.type = type;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public String toString() {
		return "DictionaryEntry [id=" + id + ", name=" + name + ", type="
				+ type + ", description=" + description + "]";
	}
}
