package org.gridkit.litter.fragmenter;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class Main {

	public static void main(String[] args) {	
		long heap = 256l << 20;
		int initialSize = 64;
		int pace = 500000;
//		BaseFragmenter frag = new ByteArrayFragmenter(heap, initialSize, pace);		
//		BaseFragmenter frag = new UnsafeFragmenter(heap, initialSize, pace);		
		GarbageSegment frag = new ByteBufferFragmenter(heap, initialSize, pace);		
		frag.execute();		
	}	
}
