package com.medx.proxy.test.model;

import com.medx.framework.type.annotation.DictType;

@DictType(packageCutPrefix = "com.medx.proxy.test")
public interface OrderItem {
	String getTitle();
	
	void setTitle(String title);
	
	double getPrice();
	
	void setPrice(double price);
}
