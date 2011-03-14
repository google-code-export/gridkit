package com.medx.framework.metadata;

public abstract class AttrKey {
	protected final int id;
	protected final String name;
	protected final int version;
	protected final String description;

	AttrKey(int id, String name, int version, String description) {
		this.id = id;
		this.name = name;
		this.version = version;
		this.description = description == null ? "" : description;
	}
	
	public AttrKey(int id, String name, int version) {
		this(id, name, version, "");
	}

	public int getId() {
		return id;
	}
	
	public int getVersion() {
		return version;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
}
