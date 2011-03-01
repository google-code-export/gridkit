package com.medx.framework.proxy.test.model;

import com.medx.framework.type.annotation.DictType;

@DictType(packageCutPrefix = "com.medx.framework.proxy.test")
public interface Customer {
	String getName();
	
	void setName(String name);
	
	int[] getTags();
	
	String[] getTitles();
}
