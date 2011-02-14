package com.medx.model.accumulator;

import com.medx.metamodel.Facade;

@Facade
public interface Rule {

	public AccumulatorCriteria getAccumulatorCriteria();
	
	public ClaimCriteria getClaimCriteria();
	
	public AccumulationMatrix getMatrix();
	
}
