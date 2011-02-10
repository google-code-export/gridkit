package org.apteka.insurance.model.drug;

import java.util.List;

import org.apteka.insurance.model.Accumulator;

public interface DrugResponse {
	public List<String> getPurchasedDrugs();
	
	public List<String> getRejectdedDrugs();
	
	public Accumulator getAccumulator();
}
