package org.apteka.insurance.model;

import org.apteka.insurance.attribute.annotation.AttrToDict;

public interface Client {
	@AttrToDict
	String getName();
}
