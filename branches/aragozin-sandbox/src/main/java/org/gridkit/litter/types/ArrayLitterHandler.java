package org.gridkit.litter.types;

import org.gridkit.litter.processing.LitterHandler;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class ArrayLitterHandler implements LitterHandler {
	
	private static int WORD_SIZE = Integer.getInteger("litter-benchmark.word-size", 4); 
	@SuppressWarnings("unused")
	private static int OBJECT_OVERHEAD = Integer.getInteger("litter-benchmark.object-overhead", 8); 
	private static int ARRAY_OVERHEAD = Integer.getInteger("litter-benchmark.array-overhead", 12); 

	@Override
	public Object lump(Object piece1, Object piece2) {
		if (piece1 == null) { 
			return piece2;
		}
		if (piece2 == null) {
			return piece1;
		}
		
		Object[] array1 = (Object[]) piece1;
		Object[] array2 = (Object[]) piece2;
		
		for(int i = 0; i != array2.length; ++i) {
			if (array2[i] == null) {
				array2[i] = array1;
				return array2;
			}
		}
		for(int i = 0; i != array1.length; ++i) {
			if (array1[i] == null) {
				array1[i] = array2;
				return array1;
			}
		}
		throw new RuntimeException("Cannot lump!");
	}

	@Override
	public Object produce(int size) {
		size -= ARRAY_OVERHEAD;
		int arraySize = (size + WORD_SIZE - 1) / WORD_SIZE;
		if (arraySize < 1) {
			arraySize = 1;
		}
		Object[] array = new Object[arraySize];
		return array;
	}

}
