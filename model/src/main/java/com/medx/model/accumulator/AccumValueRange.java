package com.medx.model.accumulator;

import com.medx.framework.annotation.ModelClass;
import com.medx.metamodel.Facade;

@Facade
@ModelClass
public interface AccumValueRange extends AccumValueMatcher {

	public Number getLowerBound();
	
	public Number getHigherBound();
	
}
