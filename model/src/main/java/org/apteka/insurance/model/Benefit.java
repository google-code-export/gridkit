package org.apteka.insurance.model;

import java.util.Map;

import org.apteka.insurance.dictionary.generator.annotation.AttrToDict;

public interface Benefit {
	@AttrToDict
	Map<String, Integer> getDrugLimits();
	
	@AttrToDict
	Double getDiscretionValue();
}
