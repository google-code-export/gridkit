package org.apteka.insurance.model;

import java.util.Map;

import org.apteka.insurance.dictionary.generator.annotation.AttrToDict;

public interface Accumulator {
	@AttrToDict("hello")
	Map<String, Integer> getDrugLimits();
	
	@AttrToDict
	double getDiscretionValue();
}
