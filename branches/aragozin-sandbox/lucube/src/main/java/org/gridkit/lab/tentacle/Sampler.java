package org.gridkit.lab.tentacle;

public interface Sampler<M extends MonitoringTarget, S extends Sample> {
	
	public S sample(M target);

}
