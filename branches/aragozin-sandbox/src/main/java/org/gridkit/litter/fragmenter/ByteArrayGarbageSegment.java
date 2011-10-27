package org.gridkit.litter.fragmenter;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class ByteArrayGarbageSegment extends GarbageSegment {

	int[] sizeTable;
	byte[][] arrayTable;
	
	public ByteArrayGarbageSegment(long totalSize, int initialSize, int growPace) {		
		tableSize = (int) (2 * totalSize / initialSize);
		sizeTable = new int[tableSize];
		arrayTable = new byte[tableSize][];
		
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

	@Override
	void allocate(int n, int size) {
		sizeTable[n] = size;
		arrayTable[n] = new byte[size];
	}
}
