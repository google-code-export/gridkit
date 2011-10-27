package org.gridkit.litter.processing;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class LitterCounter {

	private SortedMap<Long, Long> counters = new TreeMap<Long, Long>();
	private long timeQuantum;
	
	private long aggregateCounter;
	
	public LitterCounter(long timeQuantum) {
		this.timeQuantum = timeQuantum;
	}
	
	public long getSize() {
		return aggregateCounter;
	}

	public void addLitter(long expiry, long size) {
		long slot = (expiry + timeQuantum - 1) / timeQuantum;
		Long counter = counters.get(slot);
		counter = (counter == null ? counter : size + counter);
		counters.put(slot, counter);
		aggregateCounter += size;
	}
	
	public void update(long currentTime) {
		long slot = (currentTime + timeQuantum - 1) / timeQuantum;
		for(long size : counters.headMap(slot + 1).values()) {
			aggregateCounter -= size;
		}
		counters.headMap(slot + 1).clear();
	}
}
