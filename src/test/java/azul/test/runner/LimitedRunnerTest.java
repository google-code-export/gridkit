package azul.test.runner;

import org.junit.BeforeClass;
import org.junit.Test;

import azul.test.output.DummyObservationLogger;

public class LimitedRunnerTest {
	private static class SinCalculator implements Runnable {
		public static double sinSum = 0.0;
		public static int opsCount = 0;
		
		private final int times;

		private SinCalculator(int times) {
			this.times = times;
		}

		@Override
		public void run() {
			opsCount += 1;
			
			for (int i=0; i < times; ++i)
				sinSum += Math.sin(i);
		}
	}

	private static int reqOps = 1000;
	private static int seconds = 5;
	private static int times = 1024;
	
	@Test
	public void testRate() throws Exception {
		LimitedRunner runner = new LimitedRunner(new SinCalculator(times), seconds, reqOps, new DummyObservationLogger());
		
		long t = System.currentTimeMillis();
		runner.call();
		double runTime = (System.currentTimeMillis() - t) / 1000.0;
		
		System.out.println("Time = " + runTime);
		System.out.println("Ops = " +  SinCalculator.opsCount / runTime);
		System.out.println("ReqOps = " + reqOps);
	}
	
	//@Test
	public void unlimited() throws Exception {
		UnlimitedRunner runner = new UnlimitedRunner(new SinCalculator(times), seconds, new DummyObservationLogger());
		
		long t = System.currentTimeMillis();
		runner.call();
		double runTime = (System.currentTimeMillis() - t) / 1000.0;
		
		System.out.println("Time = " + runTime);
		System.out.println("Ops = " +  SinCalculator.opsCount / runTime);
	}
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		(new LimitedRunner(new SinCalculator(1024), 5, 1000, new DummyObservationLogger())).call();
		SinCalculator.sinSum = 0;
		SinCalculator.opsCount = 0;
	}
}
