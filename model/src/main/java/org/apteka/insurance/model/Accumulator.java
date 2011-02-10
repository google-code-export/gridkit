package org.apteka.insurance.model;

import java.util.Map;

public interface Accumulator {
	Map<String, Integer> getDrugLimits();
	
	Double getDiscretionValue();
}
