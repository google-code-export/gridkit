package org.gridkit.lab.zktest;

public interface Reducer<T, V> {
	
	public void add(T object);
	
	public V reduce();

}
