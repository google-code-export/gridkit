package org.gridkit.lab.meterx;

public interface ObserverAppender extends HierarchyAppender {

	public void addField(String name, Class<?> type);

	public void importSamples(SampleReader reader);
	
}
