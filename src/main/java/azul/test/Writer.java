package azul.test;

import java.util.Random;

import azul.test.util.ArrayUtil;


import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

public class Writer implements Runnable {
	private final Cache cache;
	private final int maxCacheSize;
	
	private final int arraySize;
	private final int dispersion;
	
	private final int bulkSize;
	
	private final Random rand = new Random(System.currentTimeMillis());
	
	public Writer(Cache cache, int maxCacheSize, int arraySize, int dispersion, int bulkSize) {
		this.cache = cache;
		this.maxCacheSize = maxCacheSize;
		
		this.arraySize = arraySize;
		this.dispersion = dispersion;
		
		this.bulkSize = bulkSize;
	}

	@Override
	public void run() {
	    for (int i=0; i < bulkSize; ++i)
	    	cache.put(new Element(rand.nextInt(maxCacheSize), ArrayUtil.createRandomArray(rand, arraySize, dispersion)));
	}
}
