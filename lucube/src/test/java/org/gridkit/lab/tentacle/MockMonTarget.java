package org.gridkit.lab.tentacle;

import java.util.Collection;
import java.util.Collections;

import org.gridkit.lab.tentacle.Locator.TargetAction;
import org.gridkit.util.concurrent.SensibleTaskService;

public class MockMonTarget extends BasicMonitoringTarget implements DistributedExperiment, ActiveNode {

	private String nodename;
	
	public MockMonTarget(String nodename, ObservationHost host) {
		super(host, new SensibleTaskService("MockMonTarget"));
		this.nodename = nodename;
		
	}

	@Override
	public String getNodename() {
		return nodename;
	}

	@Override
	public Collection<String> getAllNodes() {
		return Collections.singleton(nodename);
	}

	@Override
	public void sendToRemoteNode(String nodeName, TargetAction<ActiveNode> action) {
		if (nodename.equals(nodeName)) {
			action.apply(this);
		}
		else {
			throw new IllegalArgumentException("No such node '" + nodeName + "'");
		}
	}
}
