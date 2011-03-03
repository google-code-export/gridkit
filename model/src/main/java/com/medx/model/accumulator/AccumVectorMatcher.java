package com.medx.model.accumulator;

import java.util.Collection;

import com.medx.framework.annotation.ModelClass;
import com.medx.metamodel.Facade;

@Facade
@ModelClass
public interface AccumVectorMatcher extends AccumulatorCriteria {
	
	public Collection<AccumValueMatcher> getConditions();

}
