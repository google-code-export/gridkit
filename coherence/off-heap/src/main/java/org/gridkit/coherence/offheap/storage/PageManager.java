package org.gridkit.coherence.offheap.storage;

import java.nio.ByteBuffer;


public interface PageManager {
	
	public int getPageSize();
	
	public int allocate();
	
	public byte[] read(int page, int offest, int len);

	public void write(int page, int offest, byte[] chunk);
	
	public ByteBuffer getBuffer(int pageId);
	
	public void release(int pageId);

}
