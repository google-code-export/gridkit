package org.gridkit.lab.tentacle;

import org.junit.Assert;

import org.gridkit.lab.mcube.Value;
import org.gridkit.lab.mcube.Values;
import org.gridkit.lab.tentacle.ActiveNode.ActiveNodeSource;
import org.gridkit.lab.tentacle.MonitoringSchema.MonitoringConfig;
import org.junit.Test;

public class Example {

	public interface HostType extends Sample {
		
		public static Value HOST_TYPE = Values.capture(Values.call(HostType.class).hostType());
		
		public String hostType();

		enum V implements HostType {
			CLUSTER,
			WORKER
			;
			
			@Override
			public String hostType() {
				return toString();
			}
		}
	}
	
	public interface MockMemUsage extends Samples.Value {
		
	}
	
	
	@Test
	public void verify_capture() throws InterruptedException {
		Assert.assertEquals(HostType.class.getName() + "::hostType", HostType.HOST_TYPE.toString());
	}
	
	@Test
	public void basic_schema_example() throws InterruptedException {
		
		MonitoringSchema schema = new MonitoringSchema();
		
		ActiveNodeSource mon = schema.at(ActiveNode.ALL).filter("**.MON.**");
		mon.mark(HostType.V.CLUSTER);
		
		mon.at(LocalJvmProcess.ALL)
			.mark(JvmInfo.sysProperty("user.dir"))
			.mark(memusage(10))
			.mark(memusage(20));
		
	
		
		dumpAndExecute(schema);
		
	}

//	@Test
//	public void basic_analysis_example() throws InterruptedException {
//
//		Cube cube;
//		
//				
//		Axis hcol;
//		GroupEntry ops = hcol.newGroup(cube, value);
//		Axis opds = ops.subAxis();
//		SingleEntry agg = opds.newEntry(cube, value);
//		
//		
//		Axis 
//		
//		
//		
//	}
	
	private void dumpAndExecute(MonitoringSchema schema) throws InterruptedException {

//		System.out.println(GraphUtils.dump(schema.getActionGraph()));

		MonitoringConfig pack = schema.prepare();
		
		MockSampleSink sink = new MockSampleSink("ROOT", "");
		ObservationNode onode = new ObservationNode(sink);
		MockMonTarget target = new MockMonTarget("node-a.MON", onode.createChildHost(Samples.node("node-a.MON")));

		pack.deploy(target);
		
		onode.start();
		
//		System.out.println(pack);
		
		Thread.sleep(1000);
		onode.stop();
	}	
	
	public MockMemUsage memusage(final double mibs) {
		return new MockMemUsage() {
			@Override
			public double value() {
				return Math.scalb(mibs, 20);
			}
		};
	}
}
