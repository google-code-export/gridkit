package azul.test.output;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;
import java.util.concurrent.Callable;

public class GCLogger implements Callable<Void> {
	private final long timeOut;
	
	private final ObservationLogger logger;
	
	private final List<MemoryPoolMXBean> mxBeans = ManagementFactory.getMemoryPoolMXBeans();
	
	public GCLogger(ObservationLogger logger) {
		this(100 /*ms*/, logger);
	}
	
	public GCLogger(long timeOut, ObservationLogger logger) {
		this.timeOut = timeOut;
		this.logger = logger;
	}

	@Override
	public Void call()  {
		long startTime = System.currentTimeMillis();
		
		try {
			while (!Thread.interrupted()) {
				long byteUsed = 0;
				
				for (MemoryPoolMXBean mxBean : mxBeans)
					byteUsed += mxBean.getUsage().getUsed();
				
				logger.logObservation(System.currentTimeMillis() - startTime, byteUsed);
				
				if (Thread.interrupted())
					break;
				
				Thread.sleep(timeOut);
			}
		}
		catch (InterruptedException ignored) {}
		
		logger.close();
		
		return null;
	}
}
