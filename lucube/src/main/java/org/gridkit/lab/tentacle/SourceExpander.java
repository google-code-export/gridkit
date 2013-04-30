package org.gridkit.lab.tentacle;

public interface SourceExpander<F extends MonitoringTarget, T extends MonitoringTarget, S extends Source<T>> {

	public S expand(MonitoringSchema schema, Source<F> source);
	
}
