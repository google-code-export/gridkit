package com.medx.model.accumulator;

import com.medx.metamodel.Facade;

@Facade
public interface AccumVector {

	public Number getValue(AccumKey key);
	
}
