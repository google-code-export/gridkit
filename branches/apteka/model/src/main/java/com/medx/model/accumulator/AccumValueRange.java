package com.medx.model.accumulator;

import com.medx.framework.type.annotation.DictType;
import com.medx.metamodel.Facade;

@Facade
@DictType(packageCutPrefix = "com.medx.model", javaAddPrefix = "com.medx.dictionary")
public interface AccumValueRange extends AccumValueMatcher {

	public Number getLowerBound();
	
	public Number getHigherBound();
	
}
