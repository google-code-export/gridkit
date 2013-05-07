package org.gridkit.lab.gridbeans.gridrunner;

import java.util.Collection;

public abstract class AbstractReducer<I, O> implements Reducer<I, O> {

	@Override
	public final O process(I input) {
		throw new UnsupportedOperationException();
	}

	public abstract O process(Collection<I> allInputs);	
	
}
