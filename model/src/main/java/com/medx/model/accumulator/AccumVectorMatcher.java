package com.medx.model.accumulator;

import java.util.Collection;

import com.medx.metamodel.Facade;

@Facade
public interface AccumVectorMatcher extends AccumulatorCriteria {
	
	public Collection<AccumValueMatcher> getConditions();

}
