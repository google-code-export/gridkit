package org.apteka.insurance.model;

import org.apteka.insurance.attribute.annotation.AttrToDict;

public interface Drug {
	@AttrToDict
	String getName();
	
	@AttrToDict
	Double getPrice();
}
