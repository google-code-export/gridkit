package org.gridkit.coherence.offheap.storage.memlog;

interface MemoryStoreBackend {

	public abstract ByteChunk get(int pointer);
	
	public abstract void update(int pointer, ByteChunk bytes);

	public abstract int allocate(int size, int allocNo);

	public abstract void release(int pointer);

	public abstract int collectHashesForEvacuation(int[] hashes, int len);

	public abstract boolean isMarkedForRecycle(int pp);

	public abstract long getMemUsage();

	public abstract void dumpStatistics();

	// for diagnostic reasons
	public abstract int page(int npp);

	// for diagnostic reasons
	public abstract int offset(int npp);

}