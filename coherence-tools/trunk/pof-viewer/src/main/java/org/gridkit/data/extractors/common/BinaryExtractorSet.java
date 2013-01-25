package org.gridkit.data.extractors.common;

import java.nio.ByteBuffer;

public interface BinaryExtractorSet<X extends BinaryExtractor<?>> {
	
	/**
	 * Adds extractor to a array and return its ID 
	 * @param id
	 * @param extractor
	 * @return id of added extractor
	 */
	public int addExtractor(X extractor);
	
	public Object[] extractAll(ByteBuffer buffer);

}
