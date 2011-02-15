package com.medx.attribute;

import java.io.Serializable;

public class AttrKey<T> implements Serializable {
	private static final long serialVersionUID = 2463933901280809464L;
	
	private final int id;
	private final String name;
	private final Class<T> clazz;
	private final String description;

	@SuppressWarnings("unchecked")
	public AttrKey(int id, String name, Class<?> clazz, String description) {
		this.id = id;
		this.name = name;
		this.clazz = (Class<T>) clazz;
		this.description = description;
	}
	
	public AttrKey(short id, String name, Class<T> clazz) {
		this(id, name, clazz, "");
	}

	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public Class<T> getClazz() {
		return clazz;
	}

	public String getDescription() {
		return description;
	}
}
