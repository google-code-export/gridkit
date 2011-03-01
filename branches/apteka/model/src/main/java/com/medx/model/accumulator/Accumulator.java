package com.medx.model.accumulator;

import com.medx.framework.type.annotation.DictType;
import com.medx.metamodel.Facade;

@Facade
@DictType(packageCutPrefix = "com.medx.model", javaAddPrefix = "com.medx.dictionary")
public interface Accumulator {

	public AccumKey getKey();
	
	public LookBackPeriod getLookBackPeriod();
	
}
