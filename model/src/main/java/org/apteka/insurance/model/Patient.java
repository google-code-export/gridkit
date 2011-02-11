package org.apteka.insurance.model;

import org.apteka.insurance.dictionary.generator.annotation.AttrToDict;

public interface Patient {
	@AttrToDict
	String getName();
	
	@AttrToDict
	String getClientName();
}
