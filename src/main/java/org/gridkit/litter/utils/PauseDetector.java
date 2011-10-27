package org.gridkit.litter.utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class PauseDetector extends Thread {

	public static void activate() {
		if (!System.getProperties().containsKey("pause-detector.enabled")) {
			Thread thread = new PauseDetector();
			thread.setName("PauseDetector");
			thread.setDaemon(true);
			thread.setPriority(Thread.MAX_PRIORITY);	
			thread.start();
		}
	}
	
	private long sleepNanos = Long.getLong("pause-detector.sleep-nanos", 100000); // 0.1 ms
	private long pauseThreadhold = TimeUnit.MILLISECONDS.toNanos(Integer.getInteger("pause-detector.tolerance", 50)); // 2ms 

	@SuppressWarnings("unused")
	private long startTime = 0;
	private long totalPause = 0;
	
	private long lastWake;
	
	@Override
	public void run() {
		lastWake = startTime = System.nanoTime();
		while(true) {
			long newWake = System.nanoTime();
			long pause = newWake - lastWake;
			lastWake = newWake;
			if (pause > pauseThreadhold) {
				totalPause += pause;
				double dp = (1.0d * pause) / TimeUnit.MILLISECONDS.toNanos(1);
				long cp = TimeUnit.NANOSECONDS.toMillis(totalPause);
				String msg = String.format("<<PAUSE %fms total pauses %dms>>", dp, cp);
				System.out.println(msg);
				lastWake = System.nanoTime();
			}
//			LockSupport.parkNanos(sleepNanos);
			Thread.yield();
		}
	}
}
