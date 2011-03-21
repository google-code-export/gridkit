package azul.test.runner;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import azul.test.output.ObservationLogger;

public abstract class BaseRunner implements Callable<Void> {
	public static volatile AtomicInteger closed = new AtomicInteger(0);
	
	protected final long time; //milliseconds
	protected final Runnable command;
	protected final ObservationLogger logger;
	
	protected BaseRunner(Runnable command, long seconds, ObservationLogger logger) {
		this.time = TimeUnit.SECONDS.toNanos(seconds);
		this.command = command;
		this.logger = logger;
	}
}
