package org.gridkit.litter.fragmenter;

import java.util.Random;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public abstract class GarbageSegment {

	Random rnd = new Random(0);
	int tableSize;
	
	int initialSize = 10;
	double growPace = 1/100000d;
	long sizeLimit;
	long occupied;
	
	abstract int getSize(int n);
	abstract int totalSize();
	abstract void free(int n);
	abstract void sweep();
	abstract void allocate(int n, int size);
	
	
	public void execute() {
		int lastSize = initialSize;
		
		int scale = (int)(sizeLimit >> 8);
		
		int n = 0;
		
		while(true) {
			n++;
			
			int size = (int)(initialSize + n * growPace);
			if (size != lastSize) {
				System.out.println("Chunk size avg: " + size);
				System.out.println("Occupancy " + occupied + " / " + sizeLimit);
			}
			lastSize = size;
			
			if (rnd.nextInt(scale) > (occupied >> 8)) {				
				int l = (int) (size + size * rnd.nextGaussian());
				if (l < initialSize) {
					l = initialSize / 2;
				}				
				
				if (occupied + l > sizeLimit) {
					continue;
				}
				
				int x = rnd.nextInt(tableSize);
				allocate(x, l);
				occupied += l;
			}
			else {
				int x = rnd.nextInt(tableSize);
				int xs = getSize(x);
				occupied -= xs;
				free(x);
			}
		}
	}	
}
