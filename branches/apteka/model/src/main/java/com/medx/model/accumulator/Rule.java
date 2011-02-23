package com.medx.model.accumulator;

import com.medx.metamodel.Facade;
import com.medx.type.annotation.DictType;

@Facade
@DictType
public interface Rule {

	public AccumulatorCriteria getAccumulatorCriteria();
	
	public ClaimCriteria getClaimCriteria();
	
	public AccumulationMatrix getMatrix();
	
}
