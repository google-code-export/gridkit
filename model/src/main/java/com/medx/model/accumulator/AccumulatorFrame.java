package com.medx.model.accumulator;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.xalan.xslt.Process;

import com.medx.framework.type.annotation.DictType;
import com.medx.metamodel.Facade;

@Facade()
@DictType(packageCutPrefix = "com.medx.model", javaAddPrefix = "com.medx.dictionary")
public interface AccumulatorFrame {
	public Collection<Accumulator> getAccumulators();
	
	public List<AccumulationStage> getStages();
	
	public int getAge();
	
	public Map<String, List<AccumulationStage>> getStagesMap();
	
	public void getQqqq();
	
	public Double getPrice();
	
	public Process getProcess();
	
	public int[] getInttt();
	
	public Map<String, List<AccumulationStage>>[] getStagesMapArray();
}
