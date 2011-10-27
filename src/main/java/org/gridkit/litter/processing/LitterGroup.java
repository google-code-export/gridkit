package org.gridkit.litter.processing;

import java.util.Random;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class LitterGroup {
	
	private LitterQueue queue;
	private LitterCounter counter;
	private LitterHandler handler;
	private Random random;
	
	private int size;
	private int sizeDeviation;
	private long timeToLive;
	private long timeToLiveDeviation;
	private double productionRate; // byte production rate
	
	private long tickCounter;
	private long totallyProcudedObjects;
	private long totallyProcudedBytes;

	public LitterQueue getQueue() {
		return queue;
	}

	public void setQueue(LitterQueue queue) {
		this.queue = queue;
	}

	public void execute(long endTick) {
		while(tickCounter < endTick) {			
			long x = (long) (productionRate * tickCounter);
			while(totallyProcudedBytes < x) {
				int size = this.size - sizeDeviation + random.nextInt(2 * sizeDeviation);
				long expiry = (long)(timeToLive + timeToLiveDeviation * random.nextGaussian());
				
				Object litter = handler.produce(size);
				counter.addLitter(expiry, size);
				queue.addLitter(expiry, litter);
				
				totallyProcudedBytes += size;
				totallyProcudedObjects += 1;				
			}			
			++tickCounter;
		}
		counter.update(tickCounter);
	}
}
