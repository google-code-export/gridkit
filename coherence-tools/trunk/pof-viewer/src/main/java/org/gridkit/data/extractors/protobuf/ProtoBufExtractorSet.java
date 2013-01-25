package org.gridkit.data.extractors.protobuf;

import java.io.Serializable;
import java.nio.ByteBuffer;

import org.gridkit.data.extractors.common.BinaryExtractorSet;

public class ProtoBufExtractorSet<X extends ProtoBufExtractor<?>> implements BinaryExtractorSet<X>, Serializable {

	private int numExtractors;
	private Entry root;
	
	@Override
	public int addExtractor(X extractor) {
		int[] path = extractor.getPath();
		return 0;
	}
	
	@Override
	public Object[] extractAll(ByteBuffer buffer) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static class Entry implements Serializable {
		
	}
}
