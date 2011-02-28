package com.medx.proxy.test.model;

import com.medx.type.annotation.DictType;

@DictType(packageCutPrefix = "com.medx.proxy.test")
public interface Customer {
	String getName();
	
	void setName(String name);
	
	int[] getTags();
	
	String[] getTitles();
}
