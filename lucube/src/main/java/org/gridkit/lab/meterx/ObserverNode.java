package org.gridkit.lab.meterx;

import java.util.Map;


public interface ObserverNode extends HierarchyNode {

	public Map<String, Class<?>> getColumnTypes();
	
	public SampleReader readSamples();
	
}
