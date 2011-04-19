package org.gridkit.coherence.offheap.storage.memlog;

public class InHeapBinaryStoreManager extends PagedMemoryBinaryStoreManager {

	public InHeapBinaryStoreManager(String name, int pageSize, long totalSizeLimit) {
		super(name, new PageLogManager(pageSize, (int) (totalSizeLimit / pageSize)));
	}
}
