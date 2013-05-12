package org.gridkit.lab.mcube;

import java.lang.reflect.Method;

class FieldValue implements Value {

	private String sample;
	private String field;
	private Class<?> type;
	
	public FieldValue(Method m) {
		sample = m.getDeclaringClass().getName();
		field = m.getName();
		type = convert(m.getReturnType());
	}

	private Class<?> convert(Class<?> rt) {
		return rt;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((sample == null) ? 0 : sample.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		FieldValue other = (FieldValue) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		if (sample == null) {
			if (other.sample != null)
				return false;
		} else if (!sample.equals(other.sample))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return sample + "::" + field;
	}
}
