/**
 * Copyright 2008-2010 Grid Dynamics Consulting Services, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class SpeedLimit {
	
	private final Semaphore semaphore;
	private final int premits;
	private final double delay;
	
	private SpeedLimit(int permits, int opsPerSec) {
		semaphore = new Semaphore(permits);
		this.premits = permits;
		delay = (1d * TimeUnit.SECONDS.toNanos(1)) / opsPerSec; 
	}
	
	public static SpeedLimit createSpeedLimit(int permits, int opsPerSec)
	{
		final SpeedLimit res = new SpeedLimit(permits, opsPerSec);
		
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
