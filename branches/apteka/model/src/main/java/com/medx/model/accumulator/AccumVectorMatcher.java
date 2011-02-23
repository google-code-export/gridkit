package com.medx.model.accumulator;

import java.util.Collection;

import com.medx.metamodel.Facade;
import com.medx.type.annotation.DictType;

@Facade
@DictType
public interface AccumVectorMatcher extends AccumulatorCriteria {
	
	public Collection<AccumValueMatcher> getConditions();

}
