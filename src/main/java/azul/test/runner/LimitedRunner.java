package azul.test.runner;

import java.util.concurrent.TimeUnit;

import azul.test.output.ObservationLogger;

public class LimitedRunner extends BaseRunner {
	private final static long MIN_SLEEP_TIME = 64;
	private final static int OP_VAR_FACTOR = 16;
	
	private final long operationTime;
	
	public LimitedRunner(Runnable command, long seconds, float opsPerSec, ObservationLogger logger) {
		super(command, seconds, logger);
		this.operationTime = (long)(TimeUnit.SECONDS.toNanos(1) / opsPerSec);
	}

	@Override
	public Void call() throws Exception {
		long startTime = System.nanoTime();
		
		long timeCounter = Math.abs(startTime) % Math.max(operationTime, TimeUnit.MILLISECONDS.toNanos(MIN_SLEEP_TIME));
		
		long ft,st;
		
		st = System.nanoTime();
		
		while (true) {
			if (st - startTime >= time)
				break;

			do {
				command.run();
				ft = System.nanoTime();
				
				logger.logObservation((ft - startTime) / TimeUnit.MILLISECONDS.toNanos(1), ft - st);
				
				ft = System.nanoTime();
				
				//timeCounter = timeCounter - operationTime + Math.min(ft - st, OP_VAR_FACTOR * operationTime);
				timeCounter = timeCounter - operationTime + ((ft - st) > OP_VAR_FACTOR * operationTime ? 0 : ft - st);
				
				st = ft;
			} while (timeCounter > 0 && (st - startTime) < time);
			
			if (st - startTime >= time)
				break;
			
			Thread.sleep(Math.max(MIN_SLEEP_TIME, TimeUnit.NANOSECONDS.toMillis(operationTime)));
			
			ft = System.nanoTime();
			
			timeCounter += ft - st;
			
			st = ft;
		}
		logger.close();
		
		return null;
	}
}
