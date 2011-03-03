package com.medx.test.model.customer;

import com.medx.framework.annotation.DictType;

@DictType
public interface Customer {
	String getName();
	void setName(String name);
	
	Sex getSex();
	void setSex(Sex sex);
}
