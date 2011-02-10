package org.apteka.insurance.model;

import java.util.Map;

import org.apteka.insurance.attribute.annotation.AttrToDict;

public interface Accumulator {
	@AttrToDict("hello")
	Map<String, Integer> getDrugLimits();
	
	@AttrToDict
	Double getDiscretionValue();
}
