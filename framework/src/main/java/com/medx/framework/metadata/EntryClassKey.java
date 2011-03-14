package com.medx.framework.metadata;

import java.util.Map.Entry;

public final class EntryClassKey extends ClassKey  {
	private final ClassKey keyClassKey;
	private final ClassKey valueClassKey;
	
	EntryClassKey(ClassKey keyClassKey, ClassKey valueClassKey) {
		super(ClassKeyType.ENTRY);
		
		if (keyClassKey.getType().isPrimitiveOrEntry())
			throw new IllegalArgumentException("keyClassKey");
		
		if (valueClassKey.getType().isPrimitiveOrEntry())
			throw new IllegalArgumentException("valueClassKey");
		
		this.keyClassKey = keyClassKey;
		this.valueClassKey = valueClassKey;
	}

	@Override
	public Class<?> getJavaClass() {
		return Entry.class;
	}

	public ClassKey getKeyClassKey() {
		return keyClassKey;
	}

	public ClassKey getValueClassKey() {
		return valueClassKey;
	}
}
