package com.medx.model.accumulator;

import com.medx.metamodel.Facade;

@Facade
public interface Accumulator {

	public AccumKey getKey();
	
	public LookBackPeriod getLookBackPeriod();
	
}
