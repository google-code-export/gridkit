package org.gridkit.data.extractors.common;

import java.io.IOException;

public interface StreamableObject {

	public void writeSelf(ObjectEncoderStream stream) throws IOException;

	public void readSelf(ObjectDecoderStream stream) throws IOException;
	
}
