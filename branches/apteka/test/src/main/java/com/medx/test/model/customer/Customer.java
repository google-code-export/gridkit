package com.medx.test.model.customer;

import com.medx.framework.annotation.ModelClass;

@ModelClass
public interface Customer {
	String getName();
	void setName(String name);
	
	Sex getSex();
	void setSex(Sex sex);
}
