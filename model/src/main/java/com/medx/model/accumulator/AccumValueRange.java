package com.medx.model.accumulator;

import com.medx.framework.annotation.DictType;
import com.medx.metamodel.Facade;

@Facade
@DictType
public interface AccumValueRange extends AccumValueMatcher {

	public Number getLowerBound();
	
	public Number getHigherBound();
	
}
