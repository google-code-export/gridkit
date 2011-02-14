package com.medx.model.accumulator;

import com.medx.metamodel.Facade;

@Facade
public interface AccumValueRange extends AccumValueMatcher {

	public Number getLowerBound();
	
	public Number getHigherBound();
	
}
