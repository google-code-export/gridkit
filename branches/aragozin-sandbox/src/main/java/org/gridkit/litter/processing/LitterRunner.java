package org.gridkit.litter.processing;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class LitterRunner {

	private List<LitterGroup> groups;	
	private ExecutorService threads;
	
	private static class LitterTask implements Runnable {
		
		private LitterGroup group;
		private long tickLimit;
		
		@Override
		public void run() {
			group.execute(tickLimit);
			group.getQueue();
		}
	}
	
	private static class BarrierTask extends CountDownLatch implements Runnable {

		public BarrierTask(int count) {
			super(count);
		}

		@Override
		public void run() {
			countDown();
			try {
				await();
			} catch (InterruptedException e) {
				throw new RuntimeException();
			}
		}
	}
	
	private class ReportTask implements Runnable {
		
	}
}
