package com.medx.framework.metadata;

public final class UserAttrKey<T> extends AttrKey {
	public UserAttrKey(int id, String name, int version) {
		this(id, name, version, "");
	}
	
	public UserAttrKey(int id, String name, int version, String description) {
		super(id, name, version, description);
	}
}
