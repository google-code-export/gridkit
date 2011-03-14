package com.medx.framework.metadata;

public final class TypedAttrKey extends AttrKey {
	private final ClassKey classKey;
	
	TypedAttrKey(int id, String name, int version, ClassKey classKey) {
		this(id, name, version, "", classKey);
	}
	
	TypedAttrKey(int id, String name, int version, String description, ClassKey classKey) {
		super(id, name, version, description);
		
		if (classKey == null)
			throw new IllegalArgumentException("classKey");
		
		this.classKey = classKey;
	}

	public ClassKey getClassKey() {
		return classKey;
	}
}
