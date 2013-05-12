package org.gridkit.lab.mcube;

import java.util.Collection;

public interface Function extends Value {

	public Collection<Value> getArguments();
	
	public Object apply(Row row);
	
}
