package com.medx.framework.metadata;

public abstract class ClassKey {
	private final ClassKeyType type;
	
	ClassKey(ClassKeyType type) {
		if (type == null)
			throw new IllegalArgumentException("type");
		
		this.type = type;
	}

	public abstract Class<?> getJavaClass();
	
	public int getId() {
		throw new UnsupportedOperationException();
	}
	
	public int getVersion() {
		throw new UnsupportedOperationException();
	}
	
	public final ClassKeyType getType() {
		return type;
	}
	
	public ClassKey getElementKey() {
		throw new UnsupportedOperationException();
	}
	
	public final EntryClassKey getMapElementKey() {
		if (type != ClassKeyType.MAP)
			throw new UnsupportedOperationException();
		else
			return (EntryClassKey)getElementKey();
	}
}

