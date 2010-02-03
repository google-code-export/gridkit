package com.griddynamics.gridkit.coherence.patterns.benchmark.speedlimit;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class SpeedLimitImpl implements SpeedLimit
{
	private final Semaphore semaphore;
	private final int premits;
	private final double delay;
	
	private SpeedLimitImpl(int permits, int opsPerSec) {
		semaphore = new Semaphore(permits);
		this.premits = permits;
		delay = (1d * TimeUnit.SECONDS.toNanos(1)) / opsPerSec; 
	}
	
	static SpeedLimitImpl createSpeedLimitImpl(int opsPerSec)
	{
		return createSpeedLimitImpl(opsPerSec / 10, opsPerSec);
	}
	
	static SpeedLimitImpl createSpeedLimitImpl(int permits, int opsPerSec)
	{
		final SpeedLimitImpl res = new SpeedLimitImpl(permits, opsPerSec);
		
		Thread th = new Thread() {
			@Override
			public void run() {
				res.recoverPermissions();
			}
		};
		
		th.setName("SpeedLimit[" + res.toString() + "]");
		th.setDaemon(true);
		th.start();
		
		return res;
	}

	@Override
	public void accure() {
		semaphore.acquireUninterruptibly();
	}
	
	private void recoverPermissions() {
		long lastTime = System.nanoTime();
		double timeBuf = 0d;
		while(true) {
			if (semaphore.availablePermits() < premits) {
				long now = System.nanoTime();
				long waitTime = now - lastTime;
				lastTime = now;
				timeBuf += waitTime;
				if (timeBuf > TimeUnit.SECONDS.toNanos(1)) {
					timeBuf = TimeUnit.SECONDS.toNanos(1);
				}
				timeBuf -= delay;
				if (timeBuf < 0) {
					waitTime = -((long)timeBuf);
					if (waitTime > 50) {
						LockSupport.parkNanos(waitTime);
					}
				}
				semaphore.release();
			}
			else {
				LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(50));
			}
		}
	}
}
