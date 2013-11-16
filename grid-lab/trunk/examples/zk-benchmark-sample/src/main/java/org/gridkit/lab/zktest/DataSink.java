package org.gridkit.lab.zktest;

public interface DataSink<V> {

	public void push(V value);
	
}
