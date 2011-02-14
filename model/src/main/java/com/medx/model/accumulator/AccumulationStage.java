package com.medx.model.accumulator;

import java.util.Collection;

import com.medx.metamodel.Facade;

@Facade
public interface AccumulationStage {

	public Collection<Rule> getRules();

}
