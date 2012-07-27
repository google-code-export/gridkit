package org.gridkit.nimble.platform.remote;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.gridkit.gatling.remoting.LocalJvmProcessFactory;
import org.gridkit.nimble.platform.RemoteAgent;
import org.gridkit.util.vicontrol.ViNode;
import org.gridkit.util.vicontrol.ViNodeConfig;
import org.gridkit.util.vicontrol.jvm.JvmNodeProvider;
import org.gridkit.util.vicontrol.jvm.JvmProps;

public class LocalAgentFactory {

	private static AtomicInteger COUNT = new AtomicInteger();

	private JvmNodeProvider nodeProvider;
	private ViNodeConfig config;
	
	public LocalAgentFactory(String... options) {
		nodeProvider = new JvmNodeProvider(new LocalJvmProcessFactory());
		config = new ViNodeConfig();
		for(String option: options) {
			JvmProps.setJvmArg(config, option);
		}
	}
	
	public RemoteAgent createAgent(String name, String... tags) {
		ViNode node = nodeProvider.createNode(name, config);
		return new ViNodeAgent(node, new HashSet<String>(Arrays.asList(tags)));
	}
}
