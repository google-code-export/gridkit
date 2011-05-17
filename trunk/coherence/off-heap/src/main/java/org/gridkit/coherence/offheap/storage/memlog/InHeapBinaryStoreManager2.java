package org.gridkit.coherence.offheap.storage.memlog;

public class InHeapBinaryStoreManager2 extends PagedMemoryBinaryStoreManager2 {

	public InHeapBinaryStoreManager2(String name, int pageSize, long totalSizeLimit) {
		super(name, new InHeapMemoryStoreBackend(pageSize, (int) (totalSizeLimit / pageSize), 2));
	}
}
