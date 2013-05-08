package org.gridkit.lab.tentacle;

import org.gridkit.util.concurrent.TaskService;

/**
 * Generic monitoring target, offering utility to manage
 * probe scheduling and life cycle.
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public interface MonitoringTarget {

	public TaskService getSharedTaskService();

	public TaskService getBoundTaskService();
	
	public ObservationHost getObservationHost();
	
}
