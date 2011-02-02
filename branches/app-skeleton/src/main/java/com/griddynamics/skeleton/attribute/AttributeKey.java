package com.griddynamics.skeleton.attribute;

import java.io.Serializable;

public final class AttributeKey<T> implements Serializable {
	private static final long serialVersionUID = 2463933901280809464L;
	
	private final short id;
	private final Class<T> clazz;
	private final String description;

	public AttributeKey(short id, Class<T> clazz, String description) {
		this.id = id;
		this.clazz = clazz;
		this.description = description;
	}
	
	public AttributeKey(short id, Class<T> clazz) {
		this(id, clazz, null);
	}

	public short getId() {
		return id;
	}
	
	public Class<T> getClazz() {
		return clazz;
	}

	public String getDescription() {
		return description;
	}
}
