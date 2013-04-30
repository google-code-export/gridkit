package org.gridkit.lab.tentacle;

import org.gridkit.lab.tentacle.ActiveNode.ActiveNodeExpander;
import org.gridkit.lab.tentacle.ActiveNode.ActiveNodeSource;
import org.junit.Test;

public class Example {

	enum HostType {
		CLUSTER,
		WORKER
	}
	
	@Test
	public void basic_example() {
		
		MonitoringSchema schema = new MonitoringSchema();
		
		schema.find(ActiveNode.ALL)
			.report(ActiveNode.HOSTNAME);
		
		ActiveNodeSource mon = schema.find(ActiveNode.ALL).filter("**.MON.**");
		mon.mark(HostType.CLUSTER);
		
		mon.find(LocalJavaProcess.ALL)
			.filter("user.dir=/local/apps/**")
			.find(AttachJmxTarget.X);
		
		mon.find(SysMon.PS)
		
		
		
		
		
	}
	
}
