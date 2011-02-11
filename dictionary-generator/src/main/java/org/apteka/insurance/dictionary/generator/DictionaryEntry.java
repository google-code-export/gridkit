package org.apteka.insurance.dictionary.generator;

import java.io.Serializable;

public class DictionaryEntry implements Serializable {
	private static final long serialVersionUID = 3701736350302492183L;
	
	private int id;
	private String name;
	private String type;
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

	@Override
	public String toString() {
		return "DictionaryEntry [id=" + id + ", name=" + name + ", type="
				+ type + ", description=" + description + "]";
	}
}
