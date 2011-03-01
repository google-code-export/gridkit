package com.medx.framework.test.model;

import com.medx.framework.annotation.DictType;

@DictType
public interface OrderItem {
	String getTitle();
	
	void setTitle(String title);
	
	double getPrice();
	
	void setPrice(double price);
}
