package com.medx.model.accumulator;

import com.medx.metamodel.Facade;
import com.medx.type.annotation.DictType;

@Facade
@DictType(packageCutPrefix = "com.medx.model", javaAddPrefix = "com.medx.dictionary")
public interface AccumValueRange extends AccumValueMatcher {

	public Number getLowerBound();
	
	public Number getHigherBound();
	
}
