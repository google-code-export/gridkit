package azul.test.runner;

import java.util.concurrent.TimeUnit;

import azul.test.output.ObservationLogger;

public class UnlimitedRunner extends BaseRunner {
	public UnlimitedRunner(Runnable command, long seconds, ObservationLogger logger) {
		super(command, seconds, logger);
	}

	@Override
	public Void call() throws Exception {
		long startTime = System.nanoTime();
		
		long ft,st;
		
		while (true) {
			st = System.nanoTime();
			
			if (st - startTime > time)
				break;
			
			command.run();
			
			ft = System.nanoTime();
			
			logger.logObservation((ft - startTime) / TimeUnit.MILLISECONDS.toNanos(1), ft - st);
		}
		
		logger.close();
		
		return null;
	}
}
