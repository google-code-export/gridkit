package org.gridkit.lab.tentacle;

public interface SampleSink {

	public SampleSink newChildSink(String sourceId);

	public <S extends Sample> void send(S sample);

	public <S extends Sample> void send(S sample, double timestamp);
	
}
