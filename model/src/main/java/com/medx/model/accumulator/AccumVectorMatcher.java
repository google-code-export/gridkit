package com.medx.model.accumulator;

import java.util.Collection;

import com.medx.framework.annotation.DictType;
import com.medx.metamodel.Facade;

@Facade
@DictType
public interface AccumVectorMatcher extends AccumulatorCriteria {
	
	public Collection<AccumValueMatcher> getConditions();

}
