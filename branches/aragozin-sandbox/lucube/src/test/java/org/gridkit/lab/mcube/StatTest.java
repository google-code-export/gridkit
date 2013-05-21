package org.gridkit.lab.mcube;

import org.gridkit.lab.tentacle.Sample;
import org.gridkit.lab.tentacle.SampleSink;
import org.junit.Test;

public class StatTest {

	public void generateSamples(SampleSink sink) {
		
	}
	
	@Test
	public void simple_resport() {
		
		NaiveSampleStore store = new NaiveSampleStore();
		
		generateSamples(store.getRoot());
		
		Cube cube = store.getCube();

		Filter hostBasedCpu;
		cube.filter(hostBasedCpu)
			.groupBy(HostInfo.HOSTNAME)
				.aggregate(Aggregates.mean(CpuUsage.CPU))
				.aggregate();
		
		
	}
	
	public static interface HostInfo extends Sample {
		
		public static Value HOSTNAME = Values.capture(Values.call(HostInfo.class).hostname());
		
		public String hostname();
		
	}
	
	public static interface CpuUsage extends Sample {

		public static Value CPU = Values.capture(Values.call(CpuUsage.class).cpu());
		
		public double cpu();
		
	}
}
