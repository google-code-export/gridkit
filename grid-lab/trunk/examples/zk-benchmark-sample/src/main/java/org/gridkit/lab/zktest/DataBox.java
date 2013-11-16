package org.gridkit.lab.zktest;

import java.io.Serializable;

public class DataBox<V> implements DataSink<V>, DataSource<V>, Serializable {

	private static final long serialVersionUID = 20131116L;
	
	private V value;

	public DataBox() {
	}

	public DataBox(V value) {
		this.value = value;
	}

	@Override
	public V pull() {
		return value;
	}

	@Override
	public void push(V value) {
		this.value = value;
	}
}
