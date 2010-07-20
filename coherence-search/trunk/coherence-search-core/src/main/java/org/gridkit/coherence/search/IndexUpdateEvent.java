package org.gridkit.coherence.search;

import java.util.Map;

public class IndexUpdateEvent implements Map.Entry<Object, Object> {
	
	public enum Type {INSERT, UPDATE, DELETE, NOPE};
	
	/**
	 * Marker object to denote what field value is undefined.
	 */
	public static final Object UNKNOWN = new Object();
	
	private Object key;
	private Object value;
	private Object originalValue;
	private Type type;
	
	public IndexUpdateEvent(Object key, Object value, Object originalValue,	Type type) {
		this.key = key;
		this.value = value;
		this.originalValue = originalValue;
		this.type = type;
	}

	public Object getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}

	public Object getOriginalValue() {
		return originalValue;
	}

	public Type getType() {
		return type;
	}

	@Override
	public Object setValue(Object value) {
		throw new UnsupportedOperationException();
	}

	public void merge(IndexUpdateEvent event) {
		switch (type) {
			case NOPE:
				this.type = event.type;
				this.value = event.value;
				this.originalValue = event.originalValue;
				break;
			case INSERT:
				if (event.type == Type.DELETE) {
					this.type = Type.NOPE;
					this.value = null;
					this.originalValue = null;
				}
				else {
					this.value = event.value;
				}
				break;
			case UPDATE:
				if (event.type == Type.DELETE) {
					this.type = Type.DELETE;
					this.value = null;
				}
				else {
					this.value = event.value;
				}
				break;
			case DELETE:
				if (event.type == Type.INSERT) {
					this.type = Type.UPDATE;
					this.value = event.value;
				}
				break;
		}		
	}
}
