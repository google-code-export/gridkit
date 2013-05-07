package org.gridkit.lab.tentacle;

public interface Observer<T> {

	public void observe(T tuple);

	public void observe(T tuple, double timestamp);
	
}
