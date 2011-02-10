package org.apteka.insurance.attribute;

import java.io.Serializable;

public class AttrKey<T> implements Serializable {
	private static final long serialVersionUID = 2463933901280809464L;
	
	private final int id;
	private final Class<T> clazz;
	private final String description;

	public AttrKey(int id, Class<T> clazz, String description) {
		this.id = id;
		this.clazz = clazz;
		this.description = description;
	}
	
	public AttrKey(short id, Class<T> clazz) {
		this(id, clazz, "");
	}

	public int getId() {
		return id;
	}
	
	public Class<T> getClazz() {
		return clazz;
	}

	public String getDescription() {
		return description;
	}
}
