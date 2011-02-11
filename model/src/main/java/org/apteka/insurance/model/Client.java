package org.apteka.insurance.model;

import org.apteka.insurance.dictionary.generator.annotation.AttrToDict;

public interface Client {
	@AttrToDict
	String getName();
}
