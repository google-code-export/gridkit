package com.medx.model.accumulator;

import java.util.Collection;

import com.medx.metamodel.Facade;
import com.medx.type.annotation.DictType;

@Facade
@DictType(packageCutPrefix = "com.medx.model", javaAddPrefix = "com.medx.dictionary")
public interface AccumulationStage {

	public Collection<Rule> getRules();

}
