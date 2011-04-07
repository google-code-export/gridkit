/**
 * Copyright 2011 Grid Dynamics Consulting Services, Inc.
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
package org.gridkit.coherence.txlite;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
class TxSweeper {

	private int timeTreshold = 30000; // 30 sec
	private int sizeTreshold = 1000;
	private int batchLimit = 1000;
	
	private TxSuperviser txSupervizer;
	private Thread thread;
	private boolean stopped;
	
	public TxSweeper(TxSuperviser ts) {
		txSupervizer = ts;
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				go();
			}
		});
		thread.setDaemon(true);
	}

	protected void go() {
		while(!stopped) {
			if (txSupervizer.accureMaintenanceLock(300)) {
				try {
					maintenanceCycle();
				}
				finally {
					txSupervizer.releaseMaintenanceLock();
				}
			}
		}
	}
	
	private void maintenanceCycle() {
		long lastRun = 0;
		while(!stopped) {
			long waitTime = (System.currentTimeMillis() - lastRun);
			if (txSupervizer.getTxLogSize() > sizeTreshold || waitTime >= timeTreshold) {
				txSupervizer.cleanUpVersions(batchLimit);
				lastRun = System.currentTimeMillis();
				continue;
			}
			LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(timeTreshold / 2));
		}
	}

	public synchronized void start() {
		if (stopped) {
			throw new IllegalStateException("Thread is already stopped");
		}
		thread.start();
	}
	
	public synchronized void stop() {
		stopped = true;
		thread.interrupt();
	}
}
