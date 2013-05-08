package org.gridkit.lab.tentacle;

public interface Probe<S extends Sample, T extends MonitoringTarget> {
	
	public void deploy(T target);

	public void deploy(T target, double rate);
}
