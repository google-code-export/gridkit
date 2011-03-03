package com.medx.model.accumulator;

import com.medx.framework.annotation.ModelClass;
import com.medx.metamodel.Facade;

@Facade
@ModelClass
public interface Rule {

	public AccumulatorCriteria getAccumulatorCriteria();
	
	public ClaimCriteria getClaimCriteria();
	
	public AccumulationMatrix getMatrix();
	
}
