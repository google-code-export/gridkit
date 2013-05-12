package org.gridkit.lab.mcube;

import java.util.Iterator;

public interface AdditiveReducer {

	public Value getSource();
	
	public Object reduceSamples(Iterator<Object> values);

	public Object reduceReductions(Iterator<Object> values);
	
}
