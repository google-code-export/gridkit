package azul.test;

import azul.test.data.Record;

import java.util.Random;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: arechinsky
 * Date: 28.03.2011
 * Time: 18:51:44
 * To change this template use File | Settings | File Templates.
 */
public class MapReader implements Runnable {
	public static int sum = 0;

	private final Map<Integer, Record> cache;
	private final int maxCacheSize;

	private final int bulkSize;

	private final Random rand = new Random(System.currentTimeMillis());

	public MapReader(Map<Integer, Record> cache, int maxCacheSize, int bulkSize) {
		this.cache = cache;
		this.maxCacheSize = maxCacheSize;
		this.bulkSize = bulkSize;
	}

	@Override
	public void run() {
    	for (int i=0; i < bulkSize; ++i) {
    		int key = rand.nextInt(maxCacheSize);
    		Record record = cache.get(key);

    		while (record == null) {
    			key = (key + 1) % maxCacheSize;
    			record = cache.get(key);
    		}

    		sum += record.getId();
    	}

	}
}
