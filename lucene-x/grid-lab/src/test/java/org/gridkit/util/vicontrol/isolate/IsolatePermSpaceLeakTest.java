package org.gridkit.util.vicontrol.isolate;

import org.gridkit.util.vicontrol.ViHost;
import org.testng.TestNG;
import org.testng.annotations.Test;

public class IsolatePermSpaceLeakTest {

	@Test
	public void test_permanent_leak() {

		// 100 is a bit too short, but test should not take forever
		int interations = 100;
		for(int i = 0; i != interations; ++i) {
			ViHost node = new IsolateViHost("node-" + i);
			IsolateViHost.includePackage(node, "org.gridkit");
			IsolateViHost.includePackage(node, "org.testng");
			
			node.exec(new Runnable() {
				@Override
				public void run() {
					// TODO initialize something more class heavy
					new TestNG().getAnnotationTransformer();
					new TestNG().addClassLoader(getClass().getClassLoader());
					new TestNG().getConfigFailurePolicy();
					new TestNG().getReporters();
				}
			});
			
			node.shutdown();
		}
	}
	
}
