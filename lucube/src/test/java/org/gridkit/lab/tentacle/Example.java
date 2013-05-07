package org.gridkit.lab.tentacle;

import org.gridkit.lab.gridbeans.GraphUtils;
import org.gridkit.lab.tentacle.ActiveNode.ActiveNodeSource;
import org.junit.Test;

public class Example {

	public interface HostType extends Sample {
		public String hostType();
	}
	
	enum HostTypes implements HostType {
		CLUSTER,
		WORKER
		;

		@Override
		public String hostType() {
			return toString();
		}
	}
	
	@Test
	public void basic_example() {
		
		MonitoringSchema schema = new MonitoringSchema();
		
		schema.at(ActiveNode.ALL)
			.mark(ActiveNode.HOSTNAME);
		
		ActiveNodeSource mon = schema.at(ActiveNode.ALL).filter("**.MON.**");
		mon.mark(HostTypes.CLUSTER);
		
		mon.at(LocalJavaProcess.ALL)
			.filter("user.dir=/local/apps/**")
			.at(AttachJmxTarget.X);
		
		System.out.println(GraphUtils.dump(schema.getActionGraph()));
	}
	
}
