package com.medx.model.accumulator;

import com.medx.framework.annotation.DictType;
import com.medx.metamodel.Facade;

@Facade
@DictType
public interface Rule {

	public AccumulatorCriteria getAccumulatorCriteria();
	
	public ClaimCriteria getClaimCriteria();
	
	public AccumulationMatrix getMatrix();
	
}
