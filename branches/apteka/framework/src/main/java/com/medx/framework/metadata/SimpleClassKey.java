package com.medx.framework.metadata;

class SimpleClassKey extends ClassKey {
	private final Class<?> javaClass;

	SimpleClassKey(ClassKeyType type, Class<?> javaClass) {
		super(type);
		
		if (javaClass == null)
			throw new IllegalArgumentException("javaClass");
		
		this.javaClass = javaClass;
	}

	@Override
	public Class<?> getJavaClass() {
		return javaClass;
	}
}
