package com.medx.proxy.test.model;

import com.medx.attribute.annotation.AttrKey;
import com.medx.proxy.test.TestDictionary;

public interface Customer {
	@AttrKey(TestDictionary.Text.customerName)
	String getName();
	
	@AttrKey(TestDictionary.Text.customerName)
	void setName(String name);
}
