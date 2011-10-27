package org.gridkit.litter.fragmenter;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class UnsafeFragmenter extends GarbageSegment {

	int[] sizeTable;
	long[] pointerTable;
	
	Unsafe unsafe;
	
	public UnsafeFragmenter(long totalSize, int initialSize, int growPace) {		
		tableSize = (int) (2 * totalSize / initialSize);
		sizeTable = new int[tableSize];
		pointerTable = new long[tableSize];
		
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			unsafe = (Unsafe) f.get(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
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
		if (pointerTable[n] != 0) {
			unsafe.freeMemory(pointerTable[n]);
			pointerTable[n] = 0;
		}
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
		pointerTable[n] = unsafe.allocateMemory(size);
		if (pointerTable[n] == 0) {
			throw new OutOfMemoryError();
		}
	}
}
