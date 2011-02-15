package com.medx.model.accumulator;

import java.util.Collection;
import java.util.List;

import org.apteka.insurance.dictionary.generator.annotation.AttrToDict;

import com.medx.attribute.annotation.AttrKey;
import com.medx.metamodel.Facade;

@Facade()
public interface AccumulatorFrame {

	@AttrKey(com.medx.dictionary.accumulator.AccumulatorFrame.Text.accumulators)
	public Collection<Accumulator> getAccumulators();
	
//	@AttrKey(com.medx.dictionary.accumulator.AccumulatorFrame.Text.accumulators)
//	public void addAccumulator(Accumulator accum);
//
//	@AttrKey(com.medx.dictionary.accumulator.AccumulatorFrame.Text.accumulators)
//	public void clearAccumulators();
	
	@AttrToDict
	public void setAccumulators(Collection<Accumulator> accumulators);
	
	@AttrToDict
	public List<AccumulationStage> getStages();
	
}
