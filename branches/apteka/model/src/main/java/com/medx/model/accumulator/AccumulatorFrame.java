package com.medx.model.accumulator;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.medx.framework.annotation.ModelClass;
import com.medx.metamodel.Facade;

@Facade
@ModelClass
public interface AccumulatorFrame {
	public Collection<Accumulator> getAccumulators();
	
	public List<AccumulationStage> getStages();
	
	public int getAge();
	
	public Map<String, List<AccumulationStage>> getStagesMap();
	
	public void getQqqq();
	
	public Double getPrice();
	
	public int[] getInttt();
	
	public Map<String, List<AccumulationStage>>[] getStagesMapArray();
}
