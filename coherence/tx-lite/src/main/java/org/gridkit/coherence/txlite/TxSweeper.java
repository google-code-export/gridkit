package org.gridkit.coherence.txlite;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class TxSweeper {

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
