package org.gridkit.coherence.offheap.storage.memlog;

public interface BinaryStoreManager {
	
	public BinaryStore create();
	
	public void destroy(BinaryStore store);
	
	public void close();
	
}
