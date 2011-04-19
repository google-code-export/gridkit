package org.gridkit.coherence.offheap.storage;

import java.nio.ByteBuffer;

public class SimplePageManager implements PageManager {

	private ByteBuffer[] buffers;
	private int pageSize;	
	
	private int lower = 0;
	
	public SimplePageManager(int pageSize, int pageLimit) {
		this.pageSize = pageSize;
		this.buffers = new ByteBuffer[pageLimit];
	}

	@Override
	public int getPageSize() {
		return pageSize;
	}

	@Override
	public int allocate() {
		for(int i = lower; i != buffers.length; ++i) {
			if (buffers[i] == null) {
				buffers[i] = ByteBuffer.allocate(pageSize);
				return i;
			}
		}
		throw new RuntimeException("Page manager is out of memory");
	}

	@Override
	public ByteBuffer getBuffer(int pageId) {
		return buffers[pageId].duplicate();
	}

	
	
	@Override
	public byte[] read(int page, int offset, int len) {
		byte[] buf = new byte[len];
		ByteBuffer bb = buffers[page];
		for(int i = 0; i !=  len; ++i) {
			buf[i] = bb.get(offset + i);
		}		
		return buf;
	}

	@Override
	public void write(int page, int offset, byte[] chunk) {
		ByteBuffer bb = buffers[page];
		for(int i = 0; i != chunk.length; ++i) {
			bb.put(offset + i, chunk[i]);
		}		
	}

	@Override
	public void release(int pageId) {
		buffers[pageId] = null;
		if (lower > pageId) {
			lower = pageId;
		}
	}
}
