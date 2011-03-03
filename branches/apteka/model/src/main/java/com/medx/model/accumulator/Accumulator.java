package com.medx.model.accumulator;

import com.medx.framework.annotation.ModelClass;
import com.medx.metamodel.Facade;

@Facade
@ModelClass
public interface Accumulator {

	public AccumKey getKey();
	
	public LookBackPeriod getLookBackPeriod();
	
}
