package org.gridkit.coherence.offheap.storage.memlog;

import java.util.Iterator;

public interface BinaryStore {

	public ByteChunk get(ByteChunk key);
	
	public void put(ByteChunk key, ByteChunk value);
	
	public void remove(ByteChunk key);
	
	public Iterator<ByteChunk> keys();
	
}
