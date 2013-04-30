package org.gridkit.lab.tentacle;

public interface JmxHost extends MonitoringTarget {

	
	public interface JmxHostSource extends Source<JmxHost>, SamplerHost<JmxHostSource, JmxHost> {
		
	}
}
