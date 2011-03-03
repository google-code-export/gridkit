package com.medx.framework.test.model;

import com.medx.framework.annotation.ModelClass;

@ModelClass
public interface Customer {
	String getName();
	
	void setName(String name);
	
	int[] getTags();
	
	String[] getTitles();
}
