package org.gridkit.lab.tentacle;

import java.util.Collection;

import org.gridkit.lab.tentacle.Locator.TargetAction;

public interface DistributedExperiment extends MonitoringTarget {

	public Collection<String> getAllNodes();
	
	public void sendToRemoteNode(String nodeName, TargetAction<ActiveNode> action);
	
}
