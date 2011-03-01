package com.medx.model.accumulator;

import java.util.Collection;

import com.medx.framework.type.annotation.DictType;
import com.medx.metamodel.Facade;

@Facade
@DictType(packageCutPrefix = "com.medx.model", javaAddPrefix = "com.medx.dictionary")
public interface AccumVectorMatcher extends AccumulatorCriteria {
	
	public Collection<AccumValueMatcher> getConditions();

}
