package org.gridkit.lab.tentacle;

import java.util.Collection;

public interface DistributedExperiment extends MonitoringTarget {

	public Collection<String> getAllNodes();
	
	public void sendRemoteMonitor(String nodeName, RemoteMonitor monitor);
	
	public interface RemoteMonitor {
		
		public void deploy(MonitoringTarget localTarget);
		
	}
}
