package azul.test;

import java.util.Random;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import azul.test.data.Record;
import azul.test.data.SmartRecord;

public class Writer implements Runnable {
	private final Cache cache;
	private final int maxCacheSize;
	
	private final int recordSize;
	private final int dispersion;
	
	private final boolean useSmartRecord;
	
	private final int bulkSize;
	
	private final Random rand = new Random(System.currentTimeMillis());
	
	public Writer(Cache cache, int maxCacheSize, int recordSize, int dispersion, boolean useSmartRecord, int bulkSize) {
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
	    		cache.put(new Element(rand.nextInt(maxCacheSize), new Record(rand, recordSize, dispersion)));
	    	else
	    		cache.put(new Element(rand.nextInt(maxCacheSize), new SmartRecord(rand, recordSize, dispersion)));
	}
}
