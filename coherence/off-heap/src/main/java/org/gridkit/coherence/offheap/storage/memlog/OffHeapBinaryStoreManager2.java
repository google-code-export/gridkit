package org.gridkit.coherence.offheap.storage.memlog;

public class OffHeapBinaryStoreManager2 extends PagedMemoryBinaryStoreManager2 {

	public OffHeapBinaryStoreManager2(String name, int pageSize, long totalSizeLimit) {
		super(name, new OffHeapMemoryStoreBackend(pageSize, (int) (totalSizeLimit / pageSize), 2));
	}
}
