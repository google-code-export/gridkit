package org.gridkit.util.vicontrol.jvm;

import java.io.IOException;
import java.util.Map;

import org.gridkit.gatling.remoting.ControlledProcess;
import org.gridkit.gatling.remoting.JvmConfig;
import org.gridkit.gatling.remoting.JvmProcessFactory;
import org.gridkit.util.vicontrol.ViConfigurable;
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
//			config.get		
			ControlledProcess process = factory.createProcess(jvmConfig);
			return new JvmNode(name, config, process);
		} catch (IOException e) {
			throw new RuntimeException("Failed to create node '" + name + "'", e);
		}		
	}
	
	private static class JvmConfigInitilizer implements ViConfigurable {
		
		private JvmConfig jvmConfig;

		public JvmConfigInitilizer(JvmConfig jvmConfig) {
			this.jvmConfig = jvmConfig;
		}

		@Override
		public void setProp(String propName, String value) {
//			if (ViNodeConfig.matches(JvmProps.CP, propName)) {
//				String[] elements = value.split(";");
//				for(String element: )
//			}
//			
		}

		@Override
		public void setProps(Map<String, String> props) {
			ViNodeConfig.applyProps(this, props);			
		}

		@Override
		public void addStartupHook(String name, Runnable hook, boolean override) {
			// igonore			
		}

		@Override
		public void addShutdownHook(String name, Runnable hook, boolean override) {
			// ignore			
		}
	}
}
