package org.gridkit.lab.meterx;

public interface ObserverBuilder extends MetaBuilder {

	public void addField(String name, Class<?> type);

	public void importSamples(SampleReader reader);
	
}
