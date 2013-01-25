package org.gridkit.data.extractors.common;

import java.nio.ByteBuffer;

public interface BinaryExtractor<V> {
	
	public V extract(ByteBuffer buffer);
	
}
