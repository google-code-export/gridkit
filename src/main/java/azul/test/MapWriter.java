package azul.test;

import azul.test.data.Record;
import azul.test.data.SmartRecord;

import java.util.Random;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: arechinsky
 * Date: 28.03.2011
 * Time: 18:55:00
 * To change this template use File | Settings | File Templates.
 */
public class MapWriter implements Runnable {
	private final Map<Integer, Record> cache;
	private final int maxCacheSize;

	private final int recordSize;
	private final int dispersion;

	private final boolean useSmartRecord;

	private final int bulkSize;

	private final Random rand = new Random(System.currentTimeMillis());

	public MapWriter(Map<Integer, Record> cache, int maxCacheSize, int recordSize, int dispersion, boolean useSmartRecord, int bulkSize) {
		this.cache = cache;
		this.maxCacheSize = maxCacheSize;

		this.recordSize = recordSize;
		this.dispersion = dispersion;

		this.useSmartRecord = useSmartRecord;

		this.bulkSize = bulkSize;
	}

	@Override
	public void run() {
	    for (int i=0; i < bulkSize; ++i)
	    	if (!useSmartRecord)
	    		cache.put(rand.nextInt(maxCacheSize), new Record(rand, recordSize, dispersion));
	    	else
	    		cache.put(rand.nextInt(maxCacheSize), new SmartRecord(rand, recordSize, dispersion));
	}
}
