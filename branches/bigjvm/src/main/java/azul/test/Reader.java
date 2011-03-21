package azul.test;

import java.util.Random;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

public class Reader implements Runnable {
	public static byte sum = 0;
	
	private final Cache cache;
	private final int maxCacheSize;
	
	private final int bulkSize;
	
	private final Random rand = new Random(System.currentTimeMillis());
	
	public Reader(Cache cache, int maxCacheSize, int bulkSize) {
		this.cache = cache;
		this.maxCacheSize = maxCacheSize;
		this.bulkSize = bulkSize;
	}

	@Override
	public void run() {
    	for (int i=0; i < bulkSize; ++i) {
    		int key = rand.nextInt(maxCacheSize);
    		Element element = cache.get(key);
    		
    		while (element == null) {
    			key = (key + 1) % maxCacheSize;
    			element = cache.get(key);
    		}
    		
    		byte[] array = (byte[])element.getValue();
    		
    		sum += array[rand.nextInt(array.length)];
    	}
	}
}
