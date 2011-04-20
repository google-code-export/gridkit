package azul.test;

import azul.test.data.Record;
import azul.test.data.SmartRecord;

import java.util.Map;
import java.util.Random;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: arechinsky
 * Date: 18.04.2011
 * Time: 17:06:00
 * To change this template use File | Settings | File Templates.
 */
public class BatchWriter implements Runnable {
	private final Map<Integer, Record> cache;
	private final int maxCacheSize;

	private final int recordSize;
	private final int dispersion;

	private final boolean useSmartRecord;

	private final int bulkSize;

	private final Random rand = new Random(System.currentTimeMillis());

    private final Map<Integer, Record> tmp;

	public BatchWriter(Map<Integer, Record> cache, int maxCacheSize, int recordSize, int dispersion, boolean useSmartRecord, int bulkSize) {
		this.cache = cache;
		this.maxCacheSize = maxCacheSize;

		this.recordSize = recordSize;
		this.dispersion = dispersion;

		this.useSmartRecord = useSmartRecord;

		this.bulkSize = bulkSize;

        tmp = new HashMap<Integer, Record>(bulkSize);
	}

	@Override
	public void run() {

	    for (int i=0; i < bulkSize; ++i)
	    	if (!useSmartRecord)
	    		tmp.put(rand.nextInt(maxCacheSize), new Record(rand, recordSize, dispersion));
	    	else
	    		tmp.put(rand.nextInt(maxCacheSize), new SmartRecord(rand, recordSize, dispersion));
        cache.putAll(tmp);
        tmp.clear();
	}
}
