package org.gridkit.benchmark.gc;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import org.gridkit.lab.data.Sample;
import org.gridkit.lab.data.SampleList;
import org.junit.Test;

public class AutoLabTest {

	@Test
	public void go() throws InterruptedException, ExecutionException {
		
		SampleList space = new SampleList(Collections.singletonList(new Sample()));
		space = space.withFields("size", 2, 4, 6, 7, 8, 10);
		space = space.withFields("jvm", "hs7", "hs6");
		space = space.withFields("threads", 1, 2, 3, 4, 5, 6);
		space = space.times(5);
		
		SimpleSweepStrategy strategy = new SimpleSweepStrategy(space);
		FilePersitedStrategy fps = new FilePersitedStrategy(strategy, "sweep-test.csv");
		
		SimpleController sc = new SimpleController(fps, Arrays.asList(new MockExecutor("exec-A"), new MockExecutor("exec-A")));
		sc.run();
	}
	
	private class MockExecutor implements DataPointExecutor {

		private String name;
		
		public MockExecutor(String name) {
			this.name = name;
		}
		
		@Override
		public Sample process(Sample coordinates) {
			coordinates.getDouble("size");
			coordinates.getInteger("threads");
			coordinates.setResult("X", 0);
			coordinates.setResult("host", name);
			return coordinates;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
}
