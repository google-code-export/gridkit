package org.gridkit.lab.tentacle;

import org.gridkit.lab.gridbeans.GraphUtils;
import org.gridkit.lab.tentacle.ActiveNode.ActiveNodeSource;
import org.gridkit.lab.tentacle.MonitoringSchema.MonitoringConfig;
import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.vicluster.ViNodeSet;
import org.gridkit.vicluster.ViProps;
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
		
		schema.at(ActiveNode.ALL);
		
		ActiveNodeSource mon = schema.at(ActiveNode.ALL).filter("**.MON.**");
		mon.mark(HostTypes.CLUSTER);
		
//		mon.at(LocalJavaProcess.ALL)
//			.filter("user.dir=/local/apps/**")
//			.at(AttachJmxTarget.X);
		
		dumpAndExecute(schema);
		
	}
	
	private void dumpAndExecute(MonitoringSchema schema) {

		System.out.println(GraphUtils.dump(schema.getActionGraph()));

		MonitoringConfig pack = schema.prepare();
		
		System.out.println(pack);
	}
	
}
