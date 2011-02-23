package com.medx.model.accumulator;

import com.medx.metamodel.Facade;
import com.medx.type.annotation.DictType;

@Facade
@DictType
public interface AccumValueRange extends AccumValueMatcher {

	public Number getLowerBound();
	
	public Number getHigherBound();
	
}
