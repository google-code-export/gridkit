package org.gridkit.util.vicontrol.jvm;

import java.io.IOException;

import org.gridkit.gatling.remoting.ControlledProcess;
import org.gridkit.gatling.remoting.JvmConfig;
import org.gridkit.gatling.remoting.JvmProcessFactory;
import org.gridkit.util.vicontrol.ViNode;
import org.gridkit.util.vicontrol.ViNodeConfig;
import org.gridkit.util.vicontrol.ViNodeProvider;

public class JvmNodeProvider implements ViNodeProvider {

	private JvmProcessFactory factory;
	
	public JvmNodeProvider(JvmProcessFactory factory) {
		this.factory = factory;
	}

	@Override
	public boolean verifyNodeConfig(ViNodeConfig config) {
		// TODO
		return true;
	}

	@Override
	public ViNode createNode(String name, ViNodeConfig config) {
		try {		
			JvmConfig jvmConfig = new JvmConfig();
			ControlledProcess process = factory.createProcess(jvmConfig);
			return new JvmNode(name, config, process);
		} catch (IOException e) {
			throw new RuntimeException("Failed to create node '" + name + "'", e);
		}		
	}	
}
