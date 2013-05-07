package org.gridkit.lab.tentacle;

public interface Metric<S extends Sample, T extends MonitoringTarget> {
	
	public S deploy(T target);
}
