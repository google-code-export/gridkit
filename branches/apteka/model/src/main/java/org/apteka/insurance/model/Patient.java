package org.apteka.insurance.model;

import org.apteka.insurance.attribute.annotation.AttrToDict;

public interface Patient {
	@AttrToDict
	String getName();
	
	@AttrToDict
	String getClientName();
}
