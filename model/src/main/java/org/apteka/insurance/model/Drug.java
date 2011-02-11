package org.apteka.insurance.model;

import org.apteka.insurance.dictionary.generator.annotation.AttrToDict;

public interface Drug {
	@AttrToDict
	String getName();
	
	@AttrToDict
	Double getPrice();
}
