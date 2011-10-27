package org.gridkit.litter.processing;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class LitterQueue {

	private LitterHandler handler;
	private SortedMap<Long, Object> storage = new TreeMap<Long, Object>();
	private long timeQuantum;
	
	public LitterQueue(LitterHandler handler, long timeQuantum) {
		this.handler = handler;
		this.timeQuantum = timeQuantum;
	}

	public void addLitter(long expiry, Object litter) {
		long slot = (expiry + timeQuantum - 1) / timeQuantum;
		Object root = storage.get(slot);
		root = handler.lump(expiry, litter);
		storage.put(slot, root);
	}
	
	public void cleanUp(long expiry) {
		long slot = (expiry + timeQuantum - 1) / timeQuantum;
		storage.headMap(slot + 1).clear();
	}
}
