package com.medx.framework.metadata;

final class UserClassKey extends SimpleClassKey {
	private final int id;
	private final int version;

	public UserClassKey(int id, int version, Class<?> javaClass) {
		super(ClassKeyType.USER, javaClass);
		
		if (javaClass == null)
			throw new IllegalArgumentException("javaClass");
		
		this.id = id;
		this.version = version;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public int getVersion() {
		return version;
	}
}
