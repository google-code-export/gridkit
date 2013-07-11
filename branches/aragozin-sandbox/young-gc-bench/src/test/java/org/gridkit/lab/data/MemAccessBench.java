package org.gridkit.lab.data;

import java.util.Random;

public class MemAccessBench {

	private byte[] byteMemorySpace;
	private int[] intMemorySpace;
	private int[] addresses;
	
	public MemAccessBench() {
	}
	
	public void initAllignedByteMode(Random rnd, int spaceSize, int populationSize) {
		intMemorySpace = null;
		byteMemorySpace = new byte[spaceSize];
		addresses = new int[populationSize];
		for(int i = 0; i != addresses.length; ++i) {
			addresses[i] = rnd.nextInt(byteMemorySpace.length) & (~3);
		}
	}

	public void initUnallignedByteMode(Random rnd, int spaceSize, int populationSize) {
		intMemorySpace = null;
		byteMemorySpace = new byte[spaceSize];
		addresses = new int[populationSize];
		for(int i = 0; i != addresses.length; ++i) {
			addresses[i] = rnd.nextInt(byteMemorySpace.length);
		}
	}

	public void initWordMode(Random rnd, int spaceSize, int populationSize) {
		byteMemorySpace = null;
		intMemorySpace = new int[spaceSize / 4];
		addresses = new int[populationSize];
		for(int i = 0; i != addresses.length; ++i) {
			addresses[i] = rnd.nextInt(intMemorySpace.length);
		}
	}
	
	public int measure(int spree, double[] dataSink) {
		int acc = 0;
		int n = 0;
		if (byteMemorySpace != null) {
			for(int i = 0; i != dataSink.length; ++i) {
				long s = System.nanoTime();
				for(int j = 0; j != spree; ++j) {
					n = (n + 1) % addresses.length;
					acc += byteMemorySpace[addresses[n]];
				}
				dataSink[i] = System.nanoTime() - s;
			}
		}
		else {
			for(int i = 0; i != dataSink.length; ++i) {
				long s = System.nanoTime();
				for(int j = 0; j != spree; ++j) {
					n = (n + 1) % addresses.length;
					acc += intMemorySpace[addresses[n]];
				}
				dataSink[i] = System.nanoTime() - s;
			}
		}
		return acc;
	}
}
