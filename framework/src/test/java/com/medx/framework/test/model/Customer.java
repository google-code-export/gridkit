package com.medx.framework.test.model;

import com.medx.framework.annotation.DictType;

@DictType
public interface Customer {
	String getName();
	
	void setName(String name);
	
	int[] getTags();
	
	String[] getTitles();
}
