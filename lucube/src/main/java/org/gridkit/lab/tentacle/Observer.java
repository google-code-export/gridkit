package org.gridkit.lab.tentacle;

public interface Observer<T extends Sample> {

	public void observe(T tuple);

	public void observe(T tuple, double timestamp);
	
}
