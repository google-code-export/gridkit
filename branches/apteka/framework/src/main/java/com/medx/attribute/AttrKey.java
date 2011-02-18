package com.medx.attribute;

import java.io.Serializable;

public final class AttrKey<T> implements Serializable {
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
		this.description = description == null ? "" : description;
	}
	
	public AttrKey(int id, String name, Class<?> clazz) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		
		int result = 1;
		
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		AttrKey<?> other = (AttrKey<?>)obj;
		
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		
		if (id != other.id)
			return false;
		
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		
		if (clazz == null) {
			if (other.clazz != null)
				return false;
		} else if (clazz != other.clazz)
			return false;
		
		return true;
	}
}
