package org.gridkit.util.vicontrol.jvm;

import java.util.HashMap;
import java.util.Map;

import org.gridkit.gatling.remoting.JvmProcessFactory;
import org.gridkit.util.vicontrol.ViHost;
import org.gridkit.util.vicontrol.ViNode;
import org.gridkit.util.vicontrol.ViNodeConfig;

public class JvmFactoryHost implements ViHost {

	private JvmProcessFactory factory;
	
//	private Map<String, JvmNode> nodes = new HashMap<String, JvmNode>();
	
	public JvmFactoryHost(JvmProcessFactory factory) {
		this.factory = factory;
	}

	@Override
	public ViNode allocate(String nodeName, ViNodeConfig config) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ViNode get(String nodeName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, ViNode> listNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}
}
