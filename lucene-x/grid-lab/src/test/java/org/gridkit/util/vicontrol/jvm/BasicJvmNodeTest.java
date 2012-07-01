package org.gridkit.util.vicontrol.jvm;

import org.gridkit.gatling.remoting.JvmProcessFactory;
import org.gridkit.gatling.remoting.LocalJvmProcessFactory;
import org.gridkit.util.vicontrol.ViNode;
import org.gridkit.util.vicontrol.ViNodeConfig;
import org.testng.annotations.Test;

public class BasicJvmNodeTest {

	@Test
	public void hallo_world_test() {
		
		JvmProcessFactory lpf = new LocalJvmProcessFactory();
		
		JvmNodeProvider nfactory = new JvmNodeProvider(lpf);
		
		ViNode node = nfactory.createNode("HalloWorld", new ViNodeConfig());
		
		node.exec(new Runnable() {
			@Override
			public void run() {
				System.out.println("Hallo world!");
			}
		});
		
		node.shutdown();
	}	
}
