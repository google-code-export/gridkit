package org.gridkit.gatling.utils;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleSpeedLimit implements SpeedLimit {

	private static final long OPTIMISTIC_WAIT_TIMEOUT = 50;
	
	private Semaphore semaphore = new Semaphore(0, true);
	private ReentrantLock replenishLock = new ReentrantLock();
	
	private long anchorPoint;
	private int anchorDrained;
	private double eventRate; /* events per second */
	private int replenishMark;
	private int replenishLimit;
	private double burst;
	
	public SimpleSpeedLimit(double eventRate, int replenishLimit) {
		this.anchorPoint = System.nanoTime();
		this.anchorDrained = 0;
		this.eventRate = eventRate;
		this.replenishMark = replenishLimit / 2;
		this.replenishLimit = replenishLimit;
		this.burst = eventRate > 1 ? 0.3 * Math.log10(eventRate) : 0;
		this.burst += 0.3 * eventRate * OPTIMISTIC_WAIT_TIMEOUT / TimeUnit.SECONDS.toMillis(1);
	}
	
	@Override
	public void accure() {
		if (semaphore.tryAcquire()) {
			if (semaphore.availablePermits() < replenishMark) {
				tryReplenish();
			}
			return;
		}
		else {
			while(true) {
				tryReplenish();
				try {
					if (semaphore.tryAcquire(OPTIMISTIC_WAIT_TIMEOUT, TimeUnit.MILLISECONDS)) {
						return;
					}
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
		
	}

	@Override
	public void dispose() {
		// do nothing
	}

	private void tryReplenish() {
		if (replenishLock.tryLock()) {
			try {
				while(true) {
					long now = System.nanoTime();
					long split = now - anchorPoint;
					int replenishAmount = (int) (Math.ceil(eventRate * split / TimeUnit.SECONDS.toNanos(1) + burst)) - anchorDrained;
					if (replenishAmount > 0) {
						anchorDrained += replenishAmount;
						if (replenishAmount > replenishLimit) {
							replenishAmount = replenishLimit;
						}
						
//						if (anchorDrained >= 2 * replenishLimit) {
//							// reset anchor point;
//							anchorDrained = 0;
//							anchorPoint = now;
//						}
						
						semaphore.release(replenishAmount);
						return;
					}
					else {
						long sleepTime = (long)(TimeUnit.SECONDS.toNanos(1) / eventRate);
						if (sleepTime > TimeUnit.MILLISECONDS.toNanos(10)) {
							Thread.yield();
						}
						else {
							LockSupport.parkNanos(sleepTime / 5);
						}
					}
				}
			}
			finally {
				replenishLock.unlock();
			}
		}
	}	
}
