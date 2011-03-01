package com.medx.model.accumulator;

import com.medx.framework.annotation.DictType;
import com.medx.metamodel.Facade;

@Facade
@DictType
public interface Accumulator {

	public AccumKey getKey();
	
	public LookBackPeriod getLookBackPeriod();
	
}
