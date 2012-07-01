package org.gridkit.util.vicontrol;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.gridkit.gatling.remoting.LocalJvmProcessFactory;
import org.gridkit.util.vicontrol.isolate.IsolateViNodeProvider;
import org.gridkit.util.vicontrol.jvm.JvmNodeProvider;
import org.junit.After;
import org.junit.Test;

public class ViManagerTest {

	
	private ViManager manager = createViManager();
	
	public ViManager createViManager() {
		CompositeViNodeProvider provider = new CompositeViNodeProvider();
		ViNodeProvider isolateProvider = new IsolateViNodeProvider();
		ViNodeProvider localProvider = new JvmNodeProvider(new LocalJvmProcessFactory());
		
		Map<String, String> isolateSelector = new HashMap<String, String>();
		isolateSelector.put(ViProps.NODE_TYPE, "isolate");
		provider.addProvider(isolateSelector, isolateProvider);

		Map<String, String> localSelector = new HashMap<String, String>();
		localSelector.put(ViProps.NODE_TYPE, "clone-jvm");
		provider.addProvider(localSelector, localProvider);
		
		return new ViManager(provider);
	}
	
	@After
	public void dropNodes() {
		manager.shutdown();
	}
	
	@Test
	public void test_isolate_and_local_node() {
		
		manager.node("isolate.**").setProp(ViProps.NODE_TYPE, "isolate");
		manager.node("jvm.**").setProp(ViProps.NODE_TYPE, "clone-jvm");
		
		manager.node("isolate.node1");
		manager.node("jvm.node1");
		
		List<String> ids = manager.node("**.node1").massExec(new Callable<String>(){
			@Override
			public String call() throws Exception {
				return ManagementFactory.getRuntimeMXBean().getName();
			}
		});
		ids = new ArrayList<String>(ids);
		
		String name = ManagementFactory.getRuntimeMXBean().getName();
		System.out.println("Local JVM: " + name + " Nodes' JVM: " + ids);
		
		Assert.assertEquals(2, ids.size());
		Assert.assertTrue("One of VM name should be same as this", ids.remove(name));
		Assert.assertFalse("Remaining VM name should be different", ids.remove(name));
	}
}
