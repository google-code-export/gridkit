package com.medx.model.accumulator;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.xalan.xslt.Process;

import com.medx.attribute.annotation.AttrKey;
import com.medx.metamodel.Facade;
import com.medx.type.annotation.DictType;

@Facade()
@DictType
public interface AccumulatorFrame {
	@AttrKey(com.medx.dictionary.accumulator.AccumulatorFrame.Text.accumulators)
	public Collection<Accumulator> getAccumulators();
	
//	@AttrKey(com.medx.dictionary.accumulator.AccumulatorFrame.Text.accumulators)
//	public void addAccumulator(Accumulator accum);
//
//	@AttrKey(com.medx.dictionary.accumulator.AccumulatorFrame.Text.accumulators)
//	public void clearAccumulators();
	
	public List<AccumulationStage> getStages();
	
	public int getAge();
	
	public Map<String, List<AccumulationStage>> getStagesMap();
	
	public void getQqqq();
	
	public Double getPrice();
	
	public Process getProcess();
	
	public int[] getInttt();
	
	public Map<String, List<AccumulationStage>>[] getStagesMapArray();
}
