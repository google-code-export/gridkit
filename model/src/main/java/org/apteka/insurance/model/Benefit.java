package org.apteka.insurance.model;

import java.util.Map;

public interface Benefit {
	Map<String, Integer> getDrugLimits();
	
	Double getDiscretionValue();
}
