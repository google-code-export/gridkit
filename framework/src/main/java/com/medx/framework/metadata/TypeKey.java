package com.medx.framework.metadata;

import java.io.Serializable;

public class TypeKey<T> implements Serializable {
	private static final long serialVersionUID = -4861607305537837659L;
	
	private final int id;
	private final int version;
	private final Class<T> clazz;
	
	@SuppressWarnings("unchecked")
	public TypeKey(int id, int version, Class<?> clazz) {
		this.id = id;
		this.version = version;
		this.clazz = (Class<T>)clazz;
	}

	public int getId() {
		return id;
	}

	public int getVersion() {
		return version;
	}

	public Class<T> getClazz() {
		return clazz;
	}

	@Override
	public int hashCode() {
		final int prime = 97;
		int result = 1;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + id;
		result = prime * result + version;
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
		TypeKey<?> other = (TypeKey<?>) obj;
		if (clazz == null) {
			if (other.clazz != null)
				return false;
		} else if (!clazz.equals(other.clazz))
			return false;
		if (id != other.id)
			return false;
		if (version != other.version)
			return false;
		return true;
	}
}
