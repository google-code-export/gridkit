package org.gridkit.data.extractors.common;

import java.util.List;

public interface CompositeExtractor<V> extends BinaryExtractor<V> {

	public List<BinaryExtractor<?>> getSubExtractors();
	
	public V extract(Object[] arguments);
	
}
