package org.gridkit.litter.fragmenter;

import java.nio.ByteBuffer;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class ByteBufferFragmenter extends GarbageSegment {

	int[] sizeTable;
	ByteBuffer[] arrayTable;
	
	public ByteBufferFragmenter(long totalSize, int initialSize, int growPace) {		
		tableSize = (int) (2 * totalSize / initialSize);
		sizeTable = new int[tableSize];
		arrayTable = new ByteBuffer[tableSize];
		
		this.initialSize = initialSize;
		this.sizeLimit = totalSize;
		this.growPace = 1d / growPace;		
	}

	@Override
	int getSize(int n) {
		return sizeTable[n];
	}

	@Override
	void free(int n) {
		sizeTable[n] = 0;
		arrayTable[n] = null;
	}

	@Override
	void allocate(int n, int size) {
		sizeTable[n] = size;
		arrayTable[n] = ByteBuffer.allocateDirect(size);
	}
	
	@Override
	int totalSize() {
		int size = 0;
		for (int i = 0; i != sizeTable.length; ++i) {
			size += sizeTable[i];
		}
		return size;
	}

	@Override
	void sweep() {
		for (int i = 0; i != sizeTable.length; ++i) {
			if (sizeTable[i] > 0) {
				free(i);
			}
		}
	}
}
