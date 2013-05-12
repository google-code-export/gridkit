package org.gridkit.lab.tentacle;

public interface MonitoringTargetFilter<T extends MonitoringTarget> {

	/**
	 * Filter may not access to scheduler services or {@link ObservationHost} of
	 * monitoring target.
	 * 
	 * @return <code>true</code> if target matches filter predicate
	 */
	public boolean evaluate(T target);
	
}
